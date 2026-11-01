package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.models.Customer;
import com.jasonwj.snailpay.models.EmailVerificationToken;
import com.jasonwj.snailpay.repository.CustomerRepository;
import com.jasonwj.snailpay.repository.EmailVerificationTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_SENDS_PER_HOUR = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final CustomerRepository customerRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailSender emailSender;

    @Value("${snailpay.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("classpath:templates/verification-email.html")
    private Resource templateResource;

    private String template;

    @PostConstruct
    void loadTemplate() throws IOException {
        template = StreamUtils.copyToString(templateResource.getInputStream(), StandardCharsets.UTF_8);
    }

    // The email is sent inside the transaction: if the mail provider fails, the
    // new token rolls back and the user can retry without waiting out the cooldown.
    @Transactional
    public Mono<Void> sendVerificationEmail(String externalId) {
        return findCustomer(externalId).flatMap(customer -> {
            if (customer.getEmailVerifiedAt() != null) {
                return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Email already verified"));
            }

            String rawToken = generateRawToken();
            Instant now = Instant.now();

            EmailVerificationToken token = new EmailVerificationToken();
            token.setCustomerId(customer.getId());
            token.setTokenHash(hash(rawToken));
            token.setExpiresAt(now.plus(TOKEN_TTL));

            String verificationUrl = frontendBaseUrl + "/verify-email?token=" + rawToken;
            String html = renderTemplate(customer, verificationUrl);

            return enforceSendLimits(customer.getId())
                    // Invalidate earlier links before issuing the new one.
                    .then(tokenRepository.expireActiveTokens(customer.getId(), now))
                    .then(tokenRepository.save(token))
                    .then(emailSender.send(customer.getEmail(), "Verify your email", html));
        });
    }

    @Transactional
    public Mono<Void> confirmEmail(String rawToken) {
        Instant now = Instant.now();

        return tokenRepository.findByTokenHash(hash(rawToken))
                .switchIfEmpty(Mono.error(invalidLink()))
                .flatMap(token -> {
                    // Idempotent: a double click or a mail scanner that already
                    // consumed the link must not turn into an error for the user.
                    if (token.getVerifiedAt() != null) {
                        return Mono.empty();
                    }
                    if (now.isAfter(token.getExpiresAt())) {
                        return Mono.error(invalidLink());
                    }

                    token.setVerifiedAt(now);
                    return tokenRepository.save(token)
                            .then(customerRepository.findById(token.getCustomerId()))
                            .flatMap(customer -> {
                                if (customer.getEmailVerifiedAt() != null) {
                                    return Mono.just(customer);
                                }
                                customer.setEmailVerifiedAt(now);
                                return customerRepository.save(customer);
                            })
                            .then();
                });
    }

    private Mono<Void> enforceSendLimits(UUID customerId) {
        Instant now = Instant.now();

        Mono<Void> cooldown = tokenRepository.findFirstByCustomerIdOrderByCreatedAtDesc(customerId)
                .filter(last -> last.getCreatedAt().plus(RESEND_COOLDOWN).isAfter(now))
                .flatMap(last -> Mono.error(new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another email")));

        Mono<Void> hourly = tokenRepository
                .countByCustomerIdAndCreatedAtAfter(customerId, now.minus(Duration.ofHours(1)))
                .filter(count -> count >= MAX_SENDS_PER_HOUR)
                .flatMap(count -> Mono.error(new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS, "Too many emails requested")));

        return cooldown.then(hourly);
    }

    private Mono<Customer> findCustomer(String externalId) {
        return customerRepository.findByExternalId(externalId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")));
    }

    private ResponseStatusException invalidLink() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired link");
    }

    private String renderTemplate(Customer customer, String verificationUrl) {
        String name = customer.getFirstName() == null || customer.getFirstName().isBlank()
                ? "there"
                : HtmlUtils.htmlEscape(customer.getFirstName());
        return template
                .replace("{name}", name)
                .replace("{verificationUrl}", verificationUrl);
    }

    // 256 bits from SecureRandom, URL-safe, no padding. High entropy is why a
    // plain SHA-256 is enough to store (no HMAC or slow hash needed, unlike the
    // 6-digit phone code).
    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
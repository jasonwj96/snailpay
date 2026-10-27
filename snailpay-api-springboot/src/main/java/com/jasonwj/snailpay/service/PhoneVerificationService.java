package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.models.Customer;
import com.jasonwj.snailpay.models.PhoneVerificationCode;
import com.jasonwj.snailpay.repository.CustomerRepository;
import com.jasonwj.snailpay.repository.PhoneVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private static final int CODE_LENGTH = 6;
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_SENDS_PER_HOUR = 5;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final CustomerRepository customerRepository;
    private final PhoneVerificationCodeRepository codeRepository;
    private final SmsSender smsSender;

    @Value("${snailpay.verification.hmac-secret}")
    private String hmacSecret;

    // The SMS is sent inside the transaction on purpose: if the provider fails,
    // the new code row rolls back and the user can retry without waiting out
    // the cooldown.
    @Transactional
    public Mono<Void> sendCode(String externalId) {
        return findCustomer(externalId).flatMap(customer -> {
            if (customer.getPhoneVerifiedAt() != null) {
                return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Phone already verified"));
            }
            if (customer.getPhone() == null || customer.getPhone().isBlank()) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No phone number on file"));
            }

            String code = generateCode();
            Instant now = Instant.now();

            PhoneVerificationCode entry = new PhoneVerificationCode();
            entry.setCustomerId(customer.getId());
            entry.setPhone(customer.getPhone());
            entry.setCodeHash(hash(customer.getId(), code));
            entry.setExpiresAt(now.plus(CODE_TTL));

            return enforceSendLimits(customer.getId())
                    // Invalidate any earlier live code before issuing the new one.
                    .then(codeRepository.consumeActiveCodes(customer.getId(), now))
                    .then(codeRepository.save(entry))
                    .then(smsSender.send(
                            customer.getPhone(),
                            "Your Snailpay verification code is " + code + ". It expires in 10 minutes."));
        });
    }

    // noRollbackFor: failed attempts must still be counted even though we error.
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public Mono<Void> verifyCode(String externalId, String submittedCode) {
        return findCustomer(externalId).flatMap(customer -> {
            Instant now = Instant.now();

            return codeRepository.findByCustomerIdAndConsumedAtIsNull(customer.getId())
                    .switchIfEmpty(Mono.error(invalidCode()))
                    .flatMap(entry -> {
                        if (entry.getAttempts() >= MAX_VERIFY_ATTEMPTS) {
                            entry.setConsumedAt(now);
                            return codeRepository.save(entry).then(Mono.error(
                                    new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts")));
                        }
                        if (now.isAfter(entry.getExpiresAt())) {
                            entry.setConsumedAt(now);
                            return codeRepository.save(entry).then(Mono.error(invalidCode()));
                        }

                        entry.setAttempts((short) (entry.getAttempts() + 1));

                        boolean matches = MessageDigest.isEqual(
                                hash(customer.getId(), submittedCode).getBytes(StandardCharsets.UTF_8),
                                entry.getCodeHash().getBytes(StandardCharsets.UTF_8));

                        if (!matches) {
                            return codeRepository.save(entry).then(Mono.error(invalidCode()));
                        }

                        entry.setConsumedAt(now);
                        customer.setPhoneVerifiedAt(now);
                        return codeRepository.save(entry)
                                .then(customerRepository.save(customer))
                                .then();
                    });
        });
    }

    private Mono<Void> enforceSendLimits(UUID customerId) {
        Instant now = Instant.now();

        Mono<Void> cooldown = codeRepository.findFirstByCustomerIdOrderByCreatedAtDesc(customerId)
                .filter(last -> last.getCreatedAt().plus(RESEND_COOLDOWN).isAfter(now))
                .flatMap(last -> Mono.error(new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another code")));

        Mono<Void> hourly = codeRepository
                .countByCustomerIdAndCreatedAtAfter(customerId, now.minus(Duration.ofHours(1)))
                .filter(count -> count >= MAX_SENDS_PER_HOUR)
                .flatMap(count -> Mono.error(new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS, "Too many codes requested")));

        return cooldown.then(hourly);
    }

    private Mono<Customer> findCustomer(String externalId) {
        return customerRepository.findByExternalId(externalId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")));
    }

    private ResponseStatusException invalidCode() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired code");
    }

    private static String generateCode() {
        // nextInt(bound) is uniform, so there is no modulo bias.
        return String.format("%0" + CODE_LENGTH + "d", RANDOM.nextInt(1_000_000));
    }

    // HMAC-SHA256 keyed with a server secret and bound to the customer, so a
    // leaked table can't be brute-forced offline without the secret and a hash
    // can't be replayed for another customer.
    private String hash(UUID customerId, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((customerId + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC unavailable", e);
        }
    }
}
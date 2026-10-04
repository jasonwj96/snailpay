package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.models.Customer;
import com.jasonwj.snailpay.models.EmailVerificationToken;
import com.jasonwj.snailpay.repository.EmailVerificationTokenRepository;
import com.jasonwj.snailpay.templates.EmailVerificationTemplates;
import com.jasonwj.snailpay.util.EmailTokenGenerator;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class EmailVerificationService {

    private static final int TOKEN_TTL_HOURS = 24;
    private static final String VERIFICATION_BASE_URL = "https://snailpay.com/verify-email?token=";

    @Inject
    EmailVerificationTokenRepository tokenRepository;

    @Inject
    EmailTokenGenerator emailTokenGenerator;

    @Inject
    ReactiveMailer mailer;

    @WithTransaction
    public Uni<Void> sendVerificationEmail(Customer customer) {
        String rawToken = emailTokenGenerator.generateRawToken();
        String tokenHash = emailTokenGenerator.hash(rawToken);
        String verificationUrl = VERIFICATION_BASE_URL + rawToken;

        EmailVerificationToken token = new EmailVerificationToken();
        token.customerId = customer.id;
        token.tokenHash = tokenHash;
        token.expiresAt = Instant.now().plus(TOKEN_TTL_HOURS, ChronoUnit.HOURS);

        String customerName = String.join(" ",
                customer.suffix,
                customer.firstName,
                customer.middleName,
                customer.lastName);

        String htmlBody = EmailVerificationTemplates.Templates
                .verificationEmail(customerName, verificationUrl)
                .render();

        return tokenRepository.persist(token)
                .chain(() -> mailer.send(
                        Mail.withHtml(customer.email, "Verify your email", htmlBody)
                ))
                .replaceWithVoid();
    }

    @WithTransaction
    public Uni<Boolean> verifyEmail(String rawToken) {
        String tokenHash = emailTokenGenerator.hash(rawToken);

        return tokenRepository.find("tokenHash", tokenHash)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Invalid token"))
                .chain(token -> {
                    if (token.expiresAt.isBefore(Instant.now())) {
                        return Uni.createFrom().failure(new BadRequestException("Token expired"));
                    }
                    if (token.verifiedAt != null) {
                        return Uni.createFrom().item(false); // already used
                    }
                    token.verifiedAt = Instant.now();

                    return Customer.<Customer>findById(token.customerId)
                            .invoke(customer -> customer.emailVerifiedAt = Instant.now())
                            .replaceWith(true);
                });
    }

}
package com.jasonwj.snailpay.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Real sender for every profile except "dev". JavaMailSender is blocking, so
 * the send runs on boundedElastic. Needs spring.mail.* configured (a
 * transactional provider such as SES, Postmark or SendGrid over SMTP).
 */
@Component
@Profile("!dev")
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${snailpay.mail.from}")
    private String from;

    @Override
    public Mono<Void> send(String to, String subject, String htmlBody) {
        return Mono.fromRunnable(() -> {
                    MimeMessage message = mailSender.createMimeMessage();
                    try {
                        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
                        helper.setFrom(from);
                        helper.setTo(to);
                        helper.setSubject(subject);
                        helper.setText(htmlBody, true);
                    } catch (MessagingException e) {
                        throw new IllegalStateException("Could not build email", e);
                    }
                    mailSender.send(message);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
package com.jasonwj.snailpay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local-development stand-in: prints the verification link instead of sending
 * mail. It exposes the link, so it is restricted to the "dev" profile.
 */
@Slf4j
@Component
@Profile("dev")
public class LoggingEmailSender implements EmailSender {

    private static final Pattern HREF = Pattern.compile("href=\"([^\"]+)\"");

    @Override
    public Mono<Void> send(String to, String subject, String htmlBody) {
        return Mono.fromRunnable(() -> {
            Matcher matcher = HREF.matcher(htmlBody);
            String link = matcher.find() ? matcher.group(1) : "(no link found)";
            log.info("[DEV EMAIL] to={} subject={} link={}", to, subject, link);
        });
    }
}
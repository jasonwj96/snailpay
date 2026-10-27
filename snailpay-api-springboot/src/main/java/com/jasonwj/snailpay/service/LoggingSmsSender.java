package com.jasonwj.snailpay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Local-development stand-in. It prints the message (which contains the code),
 * so it is restricted to the "dev" profile. Production needs a real provider
 * implementation of {@link SmsSender} (Twilio Verify, SNS, etc.).
 */
@Slf4j
@Service
@Profile("dev")
public class LoggingSmsSender implements SmsSender {

    @Override
    public Mono<Void> send(String toE164Phone, String message) {
        return Mono.fromRunnable(() -> log.info("[DEV SMS] to={} message={}", toE164Phone, message));
    }
}
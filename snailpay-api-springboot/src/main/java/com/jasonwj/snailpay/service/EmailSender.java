package com.jasonwj.snailpay.service;

import reactor.core.publisher.Mono;

public interface EmailSender {

    Mono<Void> send(String to, String subject, String htmlBody);
}
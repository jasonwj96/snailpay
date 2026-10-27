package com.jasonwj.snailpay.service;

import reactor.core.publisher.Mono;


public interface SmsSender {

    Mono<Void> send(String toE164Phone, String message);
}
package com.jasonwj.snailpay.models;

import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Resolves which customer is making the request from authenticated credentials.
 * The KYC endpoints take the customer from here, never from a path or body
 * parameter, so one customer can't read or change another's identity data.
 *
 * The production implementation validates the ONBOARDING-scoped token from the
 * Authorization header (not built yet). Until it exists, only the dev stub is
 * registered, and the app fails to start outside the "dev" profile.
 */
public interface CurrentCustomerProvider {

    Mono<UUID> currentCustomerId(HttpHeaders headers);
}

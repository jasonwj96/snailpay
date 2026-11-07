package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.models.Customer;
import com.jasonwj.snailpay.models.CurrentCustomerProvider;
import com.jasonwj.snailpay.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * DEV ONLY. Trusts an X-Dev-External-Id header, which anyone can forge, so it
 * is registered only under the "dev" profile. Replace with a token-validating
 * implementation (onboarding-scoped token) before any real deployment. With no
 * implementation registered, the app refuses to start, which is the safe default.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevCurrentCustomerProvider implements CurrentCustomerProvider {

    private static final String HEADER = "X-Dev-External-Id";

    private final CustomerRepository customerRepository;

    @Override
    public Mono<UUID> currentCustomerId(HttpHeaders headers) {
        String externalId = headers.getFirst(HEADER);
        if (externalId == null || externalId.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        }
        return customerRepository.findByExternalId(externalId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated")))
                .map(Customer::getId);
    }
}

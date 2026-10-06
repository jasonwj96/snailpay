package com.jasonwj.snailpay.controller;

import com.jasonwj.snailpay.dto.CustomerRegistrationRequest;
import com.jasonwj.snailpay.dto.CustomerRegistrationResponse;
import com.jasonwj.snailpay.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CustomerRegistrationResponse> createCustomer(
            @RequestBody @Valid CustomerRegistrationRequest request) {
        return customerService.createCustomer(request);
    }
}

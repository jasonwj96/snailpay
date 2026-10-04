package com.jasonwj.snailpay.controller;

import com.jasonwj.snailpay.dto.CustomerRegistrationRequest;
import com.jasonwj.snailpay.dto.CustomerRegistrationResponse;
import com.jasonwj.snailpay.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CustomerRegistrationResponse createCustomer(CustomerRegistrationRequest request) {
        return customerService.createCustomer(request);
    }
}

package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.dto.CustomerRegistrationRequest;
import com.jasonwj.snailpay.dto.CustomerRegistrationResponse;
import com.jasonwj.snailpay.mapper.CustomerMapper;
import com.jasonwj.snailpay.repository.CustomerRepository;
import com.jasonwj.snailpay.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final IdGenerator idGenerator;

    @Transactional
    public Mono<CustomerRegistrationResponse> createCustomer(
            CustomerRegistrationRequest request) {

        return Mono.fromSupplier(() -> customerMapper.toEntity(request))
                .doOnNext(customer ->
                        customer.setExternalId(
                                idGenerator.getPublicCustomerId()
                        )
                )
                .flatMap(customerRepository::save)
                .map(customerMapper::toResponse);
    }
}
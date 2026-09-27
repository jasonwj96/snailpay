package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.dto.CustomerRegistrationRequest;
import com.jasonwj.snailpay.mapper.CustomerMapper;
import com.jasonwj.snailpay.models.Customer;
import com.jasonwj.snailpay.repository.CustomerRepository;
import com.jasonwj.snailpay.util.IdGenerator;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.exception.ConstraintViolationException;

@ApplicationScoped
public class CustomerService {

    private static final int MAX_EXTERNAL_ID_ATTEMPTS = 5;
    private static final String EXTERNAL_ID_CONSTRAINT = "customers_external_id_key";

    @Inject
    CustomerRepository customerRepository;

    @Inject
    CustomerMapper customerMapper;

    @Inject
    IdGenerator idGenerator;

    @WithTransaction
    public Uni<Customer> createCustomer(CustomerRegistrationRequest request) {
        
        Customer newCustomer = customerMapper.toEntity(request);

        return Uni.createFrom().deferred(() -> {
                    newCustomer.externalId = idGenerator.getPublicCustomerId();
                    return customerRepository.persist(newCustomer);
                })
                .onFailure(this::isExternalIdCollision)
                .retry().atMost(MAX_EXTERNAL_ID_ATTEMPTS)
                .replaceWith(newCustomer);
    }

    private boolean isExternalIdCollision(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve
                    && EXTERNAL_ID_CONSTRAINT.equals(cve.getConstraintName())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
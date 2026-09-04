package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.models.Customer;
import com.jasonwj.snailpay.repository.CustomerRepository;
import com.jasonwj.snailpay.util.IdGenerator;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CustomerService {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    IdGenerator idGenerator;

    @WithTransaction
    public Uni<Customer> createCustomer(Customer customer) {
        customer.setPublicId(idGenerator.getPublicCustomerId());
        return customerRepository.persist(customer).replaceWith(customer);
    }
}

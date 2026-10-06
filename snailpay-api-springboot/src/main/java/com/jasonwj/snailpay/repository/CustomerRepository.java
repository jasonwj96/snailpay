package com.jasonwj.snailpay.repository;

import com.jasonwj.snailpay.models.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, UUID> { }

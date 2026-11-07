package com.jasonwj.snailpay.repository;

import com.jasonwj.snailpay.models.CustomerAddress;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerAddressRepository extends ReactiveCrudRepository<CustomerAddress, UUID> {
}

package com.jasonwj.snailpay.repository;

import com.jasonwj.snailpay.models.KycProfile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface KycProfileRepository extends ReactiveCrudRepository<KycProfile, UUID> {

    Mono<Boolean> existsByTinBlindIndexAndCustomerIdNot(String tinBlindIndex, UUID customerId);
}

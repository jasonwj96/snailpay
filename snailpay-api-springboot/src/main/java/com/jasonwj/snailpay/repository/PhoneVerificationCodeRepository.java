package com.jasonwj.snailpay.repository;

import com.jasonwj.snailpay.models.PhoneVerificationCode;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface PhoneVerificationCodeRepository extends ReactiveCrudRepository<PhoneVerificationCode, UUID> {

    Mono<PhoneVerificationCode> findByCustomerIdAndConsumedAtIsNull(UUID customerId);

    Mono<PhoneVerificationCode> findFirstByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Mono<Long> countByCustomerIdAndCreatedAtAfter(UUID customerId, Instant since);

    @Modifying
    @Query("""
            UPDATE phone_verification_codes
               SET consumed_at = :now
             WHERE customer_id = :customerId
               AND consumed_at IS NULL
            """)
    Mono<Integer> consumeActiveCodes(@Param("customerId") UUID customerId, @Param("now") Instant now);
}
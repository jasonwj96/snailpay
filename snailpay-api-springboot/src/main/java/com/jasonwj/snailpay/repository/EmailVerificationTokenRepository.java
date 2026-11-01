package com.jasonwj.snailpay.repository;

import com.jasonwj.snailpay.models.EmailVerificationToken;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends ReactiveCrudRepository<EmailVerificationToken, UUID> {

    Mono<EmailVerificationToken> findByTokenHash(String tokenHash);

    Mono<EmailVerificationToken> findFirstByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Mono<Long> countByCustomerIdAndCreatedAtAfter(UUID customerId, Instant since);

    // Rows are kept (not deleted) so the cooldown and hourly limits can still
    // count them; "invalidating" an old link just expires it.
    @Modifying
    @Query("""
            UPDATE email_verification_tokens
               SET expires_at = :now
             WHERE customer_id = :customerId
               AND verified_at IS NULL
               AND expires_at > :now
            """)
    Mono<Integer> expireActiveTokens(@Param("customerId") UUID customerId, @Param("now") Instant now);
}
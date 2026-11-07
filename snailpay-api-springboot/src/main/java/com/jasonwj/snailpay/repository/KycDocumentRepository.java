package com.jasonwj.snailpay.repository;

import com.jasonwj.snailpay.models.KycDocument;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface KycDocumentRepository extends ReactiveCrudRepository<KycDocument, UUID> {

    Flux<KycDocument> findByCustomerId(UUID customerId);

    // Always look documents up by id AND owner, so a guessed id from another
    // customer returns nothing (IDOR protection).
    Mono<KycDocument> findByIdAndCustomerId(UUID id, UUID customerId);

    @Modifying
    @Query("""
            UPDATE kyc_documents
               SET status = 'SUPERSEDED'
             WHERE customer_id = :customerId
               AND document_type = :documentType
               AND status IN ('PENDING_UPLOAD', 'UPLOADED')
            """)
    Mono<Integer> supersedeActive(@Param("customerId") UUID customerId,
                                  @Param("documentType") String documentType);
}

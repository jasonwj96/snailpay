package com.jasonwj.snailpay.models;

import com.jasonwj.snailpay.enums.KycDocumentStatus;
import com.jasonwj.snailpay.enums.KycDocumentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("kyc_documents")
@Getter
@Setter
@NoArgsConstructor
public class KycDocument {

    @Id
    private UUID id;

    @Column("customer_id")
    private UUID customerId;

    @Column("document_type")
    private KycDocumentType documentType;

    private KycDocumentStatus status;

    @Column("s3_key")
    private String s3Key;

    @Column("content_type")
    private String contentType;

    @Column("size_bytes")
    private long sizeBytes;

    @Column("uploaded_at")
    private Instant uploadedAt;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
}

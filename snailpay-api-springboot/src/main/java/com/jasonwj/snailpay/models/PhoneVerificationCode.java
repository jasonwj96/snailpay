package com.jasonwj.snailpay.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("phone_verification_codes")
@Getter
@Setter
@NoArgsConstructor
public class PhoneVerificationCode {

    @Id
    private UUID id;

    @Column("customer_id")
    private UUID customerId;

    private String phone;

    @Column("code_hash")
    private String codeHash;

    private short attempts;

    @Column("expires_at")
    private Instant expiresAt;

    @Column("consumed_at")
    private Instant consumedAt;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
}
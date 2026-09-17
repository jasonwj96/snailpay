package com.jasonwj.snailpay.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mfa_factors")
public class MFAFactor extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    public UUID customerId;

    @Column(name = "type", nullable = false)
    public MFAFactorType type;

    @Column(name = "secret_encrypted", nullable = false)
    public byte[] secretEncrypted;

    @Column(name = "phone_number")
    public String phoneNumber;

    @Column(name = "is_primary", nullable = false)
    public boolean isPrimary = false;

    @Column(name = "verified_at")
    public Instant verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
package com.jasonwj.snailpay.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    public String externalId;

    @Column(name = "email", nullable = false, unique = true)
    public String email;

    @Column(name = "email_verified_at")
    public Instant emailVerifiedAt;

    @Column(name = "phone", length = 20, unique = true)
    public String phone;

    @Column(name = "phone_verified_at")
    public Instant phoneVerifiedAt;

    @Column(name = "full_name", length = 255)
    public String fullName;

    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public CustomerStatus status = CustomerStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    public KycStatus kycStatus = KycStatus.NOT_STARTED;

    @Column(name = "kyc_verified_at")
    public Instant kycVerifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
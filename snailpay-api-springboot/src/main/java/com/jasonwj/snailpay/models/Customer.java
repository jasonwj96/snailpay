package com.jasonwj.snailpay.models;

import com.jasonwj.snailpay.enums.CustomerStatus;
import com.jasonwj.snailpay.enums.KycStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    public String externalId;

    @Column(name = "email", nullable = false, unique = true)
    public String email;

    @Column(name = "email_verified_at")
    public Instant emailVerifiedAt;

    @Column(name = "phone", unique = true)
    public String phone;

    @Column(name = "phone_verified_at")
    public Instant phoneVerifiedAt;

    @Column(name = "first_name")
    public String firstName;

    @Column(name = "middle_name")
    public String middleName;

    @Column(name = "last_name")
    public String lastName;

    @Column(name = "suffix")
    public String suffix;

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
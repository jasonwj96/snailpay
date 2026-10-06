package com.jasonwj.snailpay.models;

import com.jasonwj.snailpay.enums.CustomerStatus;
import com.jasonwj.snailpay.enums.KycStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Table("customers")
public class Customer {

    @Id
    @Column("id")
    private UUID id;

    @Column("external_id")
    private String externalId;

    @Column("email")
    private String email;

    @Column("email_verified_at")
    private Instant emailVerifiedAt;

    @Column("phone")
    private String phone;

    @Column("phone_verified_at")
    private Instant phoneVerifiedAt;

    @Column("first_name")
    private String firstName;

    @Column("middle_name")
    private String middleName;

    @Column("last_name")
    private String lastName;

    @Column("suffix")
    private String suffix;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    @Column("status")
    private CustomerStatus status = CustomerStatus.PENDING;

    @Column("kyc_status")
    private KycStatus kycStatus = KycStatus.NOT_STARTED;

    @Column("kyc_verified_at")
    private Instant kycVerifiedAt;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}
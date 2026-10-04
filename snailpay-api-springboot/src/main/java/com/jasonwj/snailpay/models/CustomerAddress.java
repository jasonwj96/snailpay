package com.jasonwj.snailpay.models;

import com.jasonwj.snailpay.enums.AddressType;
import com.jasonwj.snailpay.enums.CountryCode;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.descriptor.jdbc.CharJdbcType;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "customer_addresses")
public class CustomerAddress  {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    public UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, updatable = false)
    public AddressType addressType;

    @Column(name = "line1", nullable = false)
    public String line1;

    @Column(name = "line2")
    public String line2;

    @Column(name = "city", nullable = false)
    public String city;

    @Column(name = "state_province", nullable = false)
    public String stateProvince;

    @Column(name = "postal_code", nullable = false)
    public String postalCode;

    @Enumerated(EnumType.STRING)
    @JdbcType(CharJdbcType.class)
    @Column(name = "country_code", nullable = false)
    public CountryCode countryCode;

    @Column(name = "is_current", nullable = false)
    public boolean isCurrent = true;

    @Column(name = "superseded_at")
    public Instant supersededAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
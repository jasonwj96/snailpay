package com.jasonwj.snailpay.models;

import com.jasonwj.snailpay.enums.ConsentType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consent_records")
public class ConsentRecord  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    public UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, updatable = false)
    public ConsentType consentType;

    @Column(name = "document_version", nullable = false, updatable = false)
    public String documentVersion;

    @Column(name = "accepted_at", nullable = false, updatable = false)
    public Instant acceptedAt;

    // INET has no native JPA type — stored/read as its text representation.
    @Column(name = "ip_address", updatable = false)
    public String ipAddress;

    @Column(name = "user_agent", updatable = false)
    public String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;
}
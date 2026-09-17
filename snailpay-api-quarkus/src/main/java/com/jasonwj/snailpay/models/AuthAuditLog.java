package com.jasonwj.snailpay.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_audit_logs")
public class AuthAuditLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    // Nullable — failed attempts against unknown emails have no customer to link.
    @Column(name = "customer_id", updatable = false)
    public UUID customerId;

    @Column(name = "email_attempted", updatable = false)
    public String emailAttempted;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    public AuditEventType eventType;

    // INET has no native JPA type — stored/read as its text representation.
    @Column(name = "ip_address", updatable = false)
    public String ipAddress;

    @Column(name = "user_agent", updatable = false)
    public String userAgent;

    @Column(name = "failure_reason", updatable = false)
    public String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;
}
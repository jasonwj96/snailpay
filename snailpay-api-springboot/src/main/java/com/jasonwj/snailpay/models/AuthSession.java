package com.jasonwj.snailpay.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions")
public class AuthSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    public UUID customerId;

    @Column(name = "token_hash", nullable = false)
    public String tokenHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "device_info")
    public String deviceInfo;

    @Column(name = "ip_address")
    public String ipAddress;

    @Column(name = "user_agent")
    public String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;

    @Column(name = "revoked_at")
    public Instant revokedAt;
}
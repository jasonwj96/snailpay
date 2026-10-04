package com.jasonwj.snailpay.models;

import com.jasonwj.snailpay.enums.PasswordAlgo;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_credentials")
public class AuthCredential  {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "customer_id", nullable = false, unique = true, updatable = false)
    public UUID customerId;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "password_algo", nullable = false)
    public PasswordAlgo passwordAlgo = PasswordAlgo.ARGON2ID;

    @Column(name = "password_updated_at", nullable = false)
    public Instant passwordUpdatedAt = Instant.now();

    @Column(name = "failed_login_attempts", nullable = false)
    public int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    public Instant lockedUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
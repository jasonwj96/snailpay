package com.jasonwj.snailpay.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_credential")
public class AuthCredential extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    // Stored as a raw FK, not a @OneToOne to Customer keeps this entity
    // free of lazy-loading/association-fetch concerns in the reactive stack.
    // Fetch the Customer separately when both are needed.
    @Column(name = "customer_id", nullable = false, unique = true, updatable = false)
    public UUID customerId;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

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
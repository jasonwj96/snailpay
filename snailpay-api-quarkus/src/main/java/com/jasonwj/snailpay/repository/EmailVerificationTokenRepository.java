package com.jasonwj.snailpay.repository;

import com.jasonwj.snailpay.models.EmailVerificationToken;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;

import java.util.UUID;

public class EmailVerificationTokenRepository implements
        PanacheRepositoryBase<EmailVerificationToken, UUID> {
}

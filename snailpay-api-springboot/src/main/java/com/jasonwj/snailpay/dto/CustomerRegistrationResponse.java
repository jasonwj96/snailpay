package com.jasonwj.snailpay.dto;

import com.jasonwj.snailpay.enums.CustomerStatus;
import com.jasonwj.snailpay.enums.KycStatus;

import java.time.Instant;


public record CustomerRegistrationResponse(
        String externalId,
        String email,
        String fullName,
        CustomerStatus status,
        KycStatus kycStatus,
        Instant createdAt
) {

}
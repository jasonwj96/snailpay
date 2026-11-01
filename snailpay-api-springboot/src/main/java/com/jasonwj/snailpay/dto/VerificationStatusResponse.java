package com.jasonwj.snailpay.dto;

public record VerificationStatusResponse(boolean emailVerified, boolean phoneVerified) {
}
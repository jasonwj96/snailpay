package com.jasonwj.snailpay.models;

/**
 * The plaintext code exists only here, between generation and the SMS send.
 * toString is overridden so it can't leak into logs by accident.
 */
public record IssuedPhoneCode(String phone, String code) {

    @Override
    public String toString() {
        return "IssuedPhoneCode[phone=" + phone + ", code=******]";
    }
}
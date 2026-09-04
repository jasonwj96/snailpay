package com.jasonwj.snailpay.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;

@ApplicationScoped
public class IdGenerator {

    private static final String ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final int ID_LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String getPublicCustomerId() {
        StringBuilder result = new StringBuilder(ID_LENGTH);

        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(ALPHABET.length());
            result.append(ALPHABET.charAt(index));
        }

        return "CUS-" + result;
    }
}
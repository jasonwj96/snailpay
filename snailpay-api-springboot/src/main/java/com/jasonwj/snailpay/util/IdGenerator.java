package com.jasonwj.snailpay.util;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class IdGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

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
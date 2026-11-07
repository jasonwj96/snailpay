package com.jasonwj.snailpay.models;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Field-level encryption for sensitive identity values (SSN/ITIN, ID numbers).
 * The context is authenticated but not secret: decryption fails unless the same
 * context is supplied, so a ciphertext copied onto another customer's row is
 * useless.
 */
public interface FieldEncryptor {

    Mono<byte[]> encrypt(String plaintext, Map<String, String> context);

    Mono<String> decrypt(byte[] ciphertext, Map<String, String> context);
}

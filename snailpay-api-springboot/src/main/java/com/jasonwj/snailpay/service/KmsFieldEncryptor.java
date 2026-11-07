package com.jasonwj.snailpay.service;

import com.jasonwj.snailpay.config.KycProperties;
import com.jasonwj.snailpay.models.FieldEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;

import java.util.Map;

/**
 * Encrypts directly with AWS KMS (fine for small values like an SSN; KMS limits
 * direct encryption to 4 KB). The key never leaves KMS, and every use shows up
 * in CloudTrail.
 */
@Component
@RequiredArgsConstructor
public class KmsFieldEncryptor implements FieldEncryptor {

    private final KmsClient kms;
    private final KycProperties props;

    @Override
    public Mono<byte[]> encrypt(String plaintext, Map<String, String> context) {
        return Mono.fromCallable(() -> kms.encrypt(r -> r
                                .keyId(props.kmsKeyArn())
                                .plaintext(SdkBytes.fromUtf8String(plaintext))
                                .encryptionContext(context))
                        .ciphertextBlob()
                        .asByteArray())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> decrypt(byte[] ciphertext, Map<String, String> context) {
        return Mono.fromCallable(() -> kms.decrypt(r -> r
                                .keyId(props.kmsKeyArn())
                                .ciphertextBlob(SdkBytes.fromByteArray(ciphertext))
                                .encryptionContext(context))
                        .plaintext()
                        .asUtf8String())
                .subscribeOn(Schedulers.boundedElastic());
    }
}

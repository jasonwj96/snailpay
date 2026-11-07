package com.jasonwj.snailpay.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "snailpay.kyc")
public record KycProperties(

        @NotBlank String bucket,
        @NotBlank String region,

        // ARN of the KMS key used to encrypt SSN/ID numbers (field-level).
        @NotBlank String kmsKeyArn,

        // Separate secret for the SSN blind index. Never reuse another secret.
        @NotBlank String tinIndexSecret,

        // Version stamp of the notice/consent texts shown on the KYC screen,
        // recorded with each consent so you can prove what the customer saw.
        @NotBlank String disclosureVersion,

        Duration uploadUrlTtl,
        Long maxUploadBytes
) {

    public KycProperties {
        if (uploadUrlTtl == null) {
            uploadUrlTtl = Duration.ofMinutes(5);
        }
        if (maxUploadBytes == null) {
            maxUploadBytes = 10L * 1024 * 1024;
        }
    }
}

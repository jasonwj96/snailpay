package com.jasonwj.snailpay.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

// Credentials come from the SDK's default provider chain: environment variables
// (AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY / AWS_PROFILE), then ~/.aws, then
// the instance or task role when deployed. Nothing is hardcoded here.
@Configuration
@EnableConfigurationProperties(KycProperties.class)
public class AwsConfig {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(KycProperties props) {
        return S3Client.builder().region(Region.of(props.region())).build();
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(KycProperties props) {
        return S3Presigner.builder().region(Region.of(props.region())).build();
    }

    @Bean(destroyMethod = "close")
    public KmsClient kmsClient(KycProperties props) {
        return KmsClient.builder().region(Region.of(props.region())).build();
    }
}

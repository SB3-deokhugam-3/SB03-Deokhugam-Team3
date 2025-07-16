package com.sprint.deokhugam.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${deokhugam.storage.s3.access-key}")
    private String accessKey;

    @Value("${deokhugam.storage.s3.secret-key}")
    private String secretKey;

    @Value("${deokhugam.storage.s3.region}")
    private String region;

    @Bean
    public AwsBasicCredentials awsBasicCredentials() {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }

    @Bean
    public S3Client amazonS3Client(AwsBasicCredentials awsBasicCredentials) {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsBasicCredentials awsBasicCredentials) {
        return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .build();
    }
}

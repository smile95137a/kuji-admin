package com.group.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 配置
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Configuration
public class S3Config {

    @Value("${aws.s3.region:ap-northeast-1}")
    private String region;

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    /**
     * 建立 S3 Client
     */
    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider credentialsProvider;
        
        // 如果有設定 access key，使用靜態憑證
        if (accessKey != null && !accessKey.isEmpty()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            credentialsProvider = StaticCredentialsProvider.create(credentials);
        } else {
            // 否則使用 EC2 Instance Profile（推薦用於正式環境）
            credentialsProvider = software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create();
        }

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /**
     * 建立 S3 Presigner（用於生成預簽名 URL）
     */
    @Bean
    public S3Presigner s3Presigner() {
        AwsCredentialsProvider credentialsProvider;
        
        if (accessKey != null && !accessKey.isEmpty()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            credentialsProvider = StaticCredentialsProvider.create(credentials);
        } else {
            credentialsProvider = software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create();
        }

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}

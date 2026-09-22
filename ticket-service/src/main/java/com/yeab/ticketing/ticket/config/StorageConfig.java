package com.yeab.ticketing.ticket.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.storage.enabled", havingValue = "true")
    public MinioClient minioClient(@Value("${app.storage.minio.endpoint:http://localhost:9000}") String endpoint,
                                   @Value("${app.storage.minio.access-key:minioadmin}") String accessKey,
                                   @Value("${app.storage.minio.secret-key:minioadmin}") String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
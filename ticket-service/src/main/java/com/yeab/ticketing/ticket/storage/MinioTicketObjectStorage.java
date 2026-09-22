package com.yeab.ticketing.ticket.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO-backed storage (active when {@code app.storage.enabled=true}). The
 * bucket is created lazily on startup if it does not exist.
 */
@Component
@ConditionalOnProperty(name = "app.storage.enabled", havingValue = "true")
public class MinioTicketObjectStorage implements TicketObjectStorage {

    private static final Logger log = LoggerFactory.getLogger(MinioTicketObjectStorage.class);

    private final MinioClient minioClient;
    private final String bucket;

    @Autowired
    public MinioTicketObjectStorage(MinioClient minioClient,
                                    @Value("${app.storage.minio.bucket:ticketing-tickets}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        ensureBucket();
    }

    @Override
    public String store(UUID ticketId, byte[] data) throws IOException {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(keyFor(ticketId))
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType("image/png")
                    .build());
            return keyFor(ticketId);
        } catch (Exception ex) {
            throw new IOException("MinIO put failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public byte[] get(UUID ticketId) throws IOException {
        try (InputStream in = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(keyFor(ticketId))
                .build())) {
            return in.readAllBytes();
        } catch (Exception ex) {
            throw new IOException("MinIO get failed: " + ex.getMessage(), ex);
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket '{}'", bucket);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Could not ensure MinIO bucket '" + bucket + "': " + ex.getMessage(), ex);
        }
    }
}
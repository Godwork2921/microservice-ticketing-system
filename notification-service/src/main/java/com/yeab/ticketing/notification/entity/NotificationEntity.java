package com.yeab.ticketing.notification.entity;

import com.yeab.ticketing.notification.enums.NotificationChannel;
import com.yeab.ticketing.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id private UUID id;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 200) private String idempotencyKey;
    @Column(name = "customer_id", nullable = false, length = 200) private String customerId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private NotificationChannel channel;
    @Column(nullable = false, length = 320) private String recipient;
    @Column(length = 300) private String subject;
    @Column(nullable = false, columnDefinition = "text") private String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private NotificationStatus status;
    @Column(name = "provider_message_id", length = 200) private String providerMessageId;
    @Column(name = "failure_reason", length = 500) private String failureReason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "attempt_count", nullable = false) private int attemptCount;
    @Version private long version;
    protected NotificationEntity() { }
    public NotificationEntity(String idempotencyKey, String customerId, NotificationChannel channel,
                              String recipient, String subject, String message) {
        this.idempotencyKey = idempotencyKey; this.customerId = customerId; this.channel = channel;
        this.recipient = recipient; this.subject = subject; this.message = message; this.status = NotificationStatus.PENDING;
    }
    @PrePersist void onCreate() { if (id == null) id = UUID.randomUUID(); if (createdAt == null) createdAt = Instant.now(); }
    public UUID getId() { return id; } public String getIdempotencyKey() { return idempotencyKey; }
    public String getCustomerId() { return customerId; } public NotificationChannel getChannel() { return channel; }
    public String getRecipient() { return recipient; } public String getSubject() { return subject; }
    public String getMessage() { return message; } public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; } public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }
    public String getFailureReason() { return failureReason; } public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; } public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; } public int getAttemptCount() { return attemptCount; }
    public void incrementAttemptCount() { attemptCount++; }
}

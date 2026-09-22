package com.yeab.ticketing.notification.service;

import com.yeab.ticketing.notification.dto.NotificationResponse;
import com.yeab.ticketing.notification.dto.SendNotificationRequest;
import com.yeab.ticketing.notification.entity.NotificationEntity;
import com.yeab.ticketing.notification.enums.NotificationStatus;
import com.yeab.ticketing.notification.exception.NotificationNotFoundException;
import com.yeab.ticketing.notification.mapper.NotificationMapper;
import com.yeab.ticketing.notification.messaging.NotificationEventOutbox;
import com.yeab.ticketing.notification.provider.NotificationProvider;
import com.yeab.ticketing.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repository;
    private final NotificationProvider provider;
    private final NotificationEventOutbox eventOutbox;
    public NotificationServiceImpl(NotificationRepository repository, NotificationProvider provider,
                                   NotificationEventOutbox eventOutbox) {
        this.repository = repository;
        this.provider = provider;
        this.eventOutbox = eventOutbox;
    }

    @Override @Transactional public NotificationResponse send(SendNotificationRequest request) {
        NotificationEntity existing = repository.findByIdempotencyKey(request.idempotencyKey()).orElse(null);
        if (existing != null) return NotificationMapper.toResponse(existing);
        NotificationEntity notification = new NotificationEntity(request.idempotencyKey().trim(), request.customerId().trim(), request.channel(), request.recipient().trim(), request.subject(), request.message());
        return deliver(notification);
    }
    @Override @Transactional(readOnly = true) public NotificationResponse getById(UUID id) { return NotificationMapper.toResponse(find(id)); }
    @Override @Transactional(readOnly = true) public Page<NotificationResponse> list(String customerId, Pageable pageable) { return (customerId == null || customerId.isBlank() ? repository.findAll(pageable) : repository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)).map(NotificationMapper::toResponse); }
    @Override @Transactional public NotificationResponse retry(UUID id) {
        NotificationEntity notification = find(id);
        if (notification.getStatus() == NotificationStatus.SENT) return NotificationMapper.toResponse(notification);
        return deliver(notification);
    }
    private NotificationResponse deliver(NotificationEntity notification) {
        notification.incrementAttemptCount();
        try {
            notification.setProviderMessageId(provider.send(notification));
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            NotificationEntity saved = repository.save(notification);
            eventOutbox.recordSent(saved);
            return NotificationMapper.toResponse(saved);
        } catch (RuntimeException exception) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(exception.getMessage());
            return NotificationMapper.toResponse(repository.save(notification));
        }
    }
    private NotificationEntity find(UUID id) { return repository.findById(id).orElseThrow(() -> new NotificationNotFoundException(id)); }
}
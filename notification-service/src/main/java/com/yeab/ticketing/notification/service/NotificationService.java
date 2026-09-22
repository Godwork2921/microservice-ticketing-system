package com.yeab.ticketing.notification.service;

import com.yeab.ticketing.notification.dto.NotificationResponse;
import com.yeab.ticketing.notification.dto.SendNotificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface NotificationService {
    NotificationResponse send(SendNotificationRequest request);
    NotificationResponse getById(UUID id);
    Page<NotificationResponse> list(String customerId, Pageable pageable);
    NotificationResponse retry(UUID id);
}

package com.yeab.ticketing.notification.provider;

import com.yeab.ticketing.notification.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class MockNotificationProvider implements NotificationProvider {
    @Override public String send(NotificationEntity notification) { return "mock-" + notification.getId(); }
}

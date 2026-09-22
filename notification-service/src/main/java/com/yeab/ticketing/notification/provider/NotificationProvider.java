package com.yeab.ticketing.notification.provider;

import com.yeab.ticketing.notification.entity.NotificationEntity;

public interface NotificationProvider {
    String send(NotificationEntity notification);
}

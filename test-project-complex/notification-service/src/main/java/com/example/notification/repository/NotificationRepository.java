package com.example.notification.repository;

import com.example.notification.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class NotificationRepository {
    private final Map<String, Notification> notifications = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            notification.setId("N" + String.format("%03d", idGenerator.getAndIncrement()));
        }
        notifications.put(notification.getId(), notification);
        return notification;
    }

    public Notification findById(String id) {
        return notifications.get(id);
    }
}

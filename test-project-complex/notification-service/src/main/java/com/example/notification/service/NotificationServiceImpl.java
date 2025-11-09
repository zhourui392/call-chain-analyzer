package com.example.notification.service;

import com.example.notification.api.NotificationService;
import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService(version = "1.0.0", group = "ecommerce")
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @DubboReference(version = "1.0.0", group = "ecommerce")
    private com.example.user.api.UserInfoService userInfoService;

    @Override
    public String sendNotification(Long userId, String type, String content) {
        // 获取用户联系方式
        com.example.user.model.User user = userInfoService.getUserContacts(userId);

        // 根据类型发送通知
        if ("EMAIL".equals(type) && user != null && user.getEmail() != null) {
            emailService.send(user.getEmail(), "系统通知", content);
        } else if ("SMS".equals(type) && user != null && user.getPhone() != null) {
            smsService.send(user.getPhone(), content);
        }

        // 保存通知记录
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content);
        notification.setStatus("SENT");
        notification = notificationRepository.save(notification);

        return notification.getId();
    }
}

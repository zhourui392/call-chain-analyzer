package com.example.notification.api;

/**
 * 通知服务 Dubbo 接口
 */
public interface NotificationService {

    /**
     * 发送通知
     * @param userId 用户ID
     * @param type 通知类型（EMAIL, SMS, PUSH）
     * @param content 通知内容
     * @return 通知ID
     */
    String sendNotification(Long userId, String type, String content);
}

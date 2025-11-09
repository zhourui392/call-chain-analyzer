package com.example.notification.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public boolean send(String phone, String content) {
        // 模拟发送短信
        System.out.println("Sending SMS to " + phone);
        System.out.println("Content: " + content);
        return true;
    }
}

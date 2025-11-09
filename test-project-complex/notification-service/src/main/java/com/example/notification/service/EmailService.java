package com.example.notification.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public boolean send(String email, String subject, String content) {
        // 模拟发送邮件
        System.out.println("Sending email to " + email);
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
        return true;
    }
}

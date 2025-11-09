package com.example.notification.model;

import java.util.Date;

public class Notification {
    private String id;
    private Long userId;
    private String type;
    private String content;
    private String status;
    private Date createdAt;

    public Notification() {
    }

    public Notification(String id, Long userId, String type, String content, String status) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.status = status;
        this.createdAt = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

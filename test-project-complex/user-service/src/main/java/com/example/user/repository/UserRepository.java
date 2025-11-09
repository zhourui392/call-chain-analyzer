package com.example.user.repository;

import com.example.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();

    public UserRepository() {
        // 初始化测试数据
        users.put(123L, new User(123L, "张三", "zhangsan@example.com", "13800138000", "VIP"));
        users.put(456L, new User(456L, "李四", "lisi@example.com", "13900139000", "GOLD"));
        users.put(789L, new User(789L, "王五", "wangwu@example.com", "13700137000", "SILVER"));
    }

    public User findById(Long id) {
        return users.get(id);
    }

    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }
}

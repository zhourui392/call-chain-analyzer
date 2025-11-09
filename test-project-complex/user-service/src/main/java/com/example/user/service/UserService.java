package com.example.user.service;

import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @org.apache.dubbo.config.annotation.DubboReference(version = "1.0.0", group = "ecommerce")
    private com.example.order.api.OrderService orderService;

    public User getUserWithOrders(Long id) {
        User user = userRepository.findById(id);
        if (user != null) {
            // 获取用户订单（这里不返回订单，只是触发调用）
            orderService.getOrdersByUserId(id);
        }
        return user;
    }
}

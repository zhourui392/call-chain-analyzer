package com.example.user.service;

import com.example.user.model.User;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @DubboReference(version = "1.0.0", group = "default")
    private com.example.order.api.OrderService orderService;

    public User findUserById(Long id) {
        // Simulate database lookup
        User user = new User();
        user.setId(id);
        user.setName("John Doe");
        user.setEmail("john@example.com");

        // Cross-service RPC call to order-service
        List<String> orders = orderService.getOrdersByUserId(id);
        System.out.println("User has " + orders.size() + " orders");

        return user;
    }

    public User createUser(User user) {
        // Simulate user creation
        user.setId(System.currentTimeMillis());
        return user;
    }

    public void deleteUser(Long id) {
        // Simulate deletion
        System.out.println("Deleting user: " + id);
    }
}

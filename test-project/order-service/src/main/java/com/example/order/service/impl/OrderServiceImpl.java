package com.example.order.service.impl;

import com.example.order.api.OrderService;
import com.example.order.repository.OrderRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Dubbo service implementation
 */
@DubboService(version = "1.0.0", group = "default")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<String> getOrdersByUserId(Long userId) {
        // Business logic
        return orderRepository.findByUserId(userId);
    }

    @Override
    public String createOrder(Long userId, String productId) {
        String orderId = "ORD-" + System.currentTimeMillis();
        orderRepository.save(orderId, userId, productId);
        return orderId;
    }

    @Override
    public void cancelOrder(String orderId) {
        orderRepository.delete(orderId);
    }
}

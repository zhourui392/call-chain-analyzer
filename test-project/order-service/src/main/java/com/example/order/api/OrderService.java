package com.example.order.api;

import java.util.List;

/**
 * Dubbo service interface for order operations
 */
public interface OrderService {
    List<String> getOrdersByUserId(Long userId);
    String createOrder(Long userId, String productId);
    void cancelOrder(String orderId);
}

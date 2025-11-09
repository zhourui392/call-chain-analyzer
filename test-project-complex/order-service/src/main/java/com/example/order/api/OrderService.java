package com.example.order.api;

import com.example.order.model.Order;

import java.util.List;

/**
 * 订单服务 Dubbo 接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(Long userId, String productId, Integer quantity, String address);

    /**
     * 获取用户订单列表
     */
    List<Order> getOrdersByUserId(Long userId);

    /**
     * 获取用户订单历史（用于推荐）
     */
    List<String> getUserOrderHistory(Long userId);
}

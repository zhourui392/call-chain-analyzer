package com.example.order.repository;

import com.example.order.model.Order;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class OrderRepository {
    private final Map<String, Order> orders = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public Order save(Order order) {
        if (order.getId() == null) {
            String dateStr = "20231109";
            order.setId("O" + dateStr + String.format("%03d", idGenerator.getAndIncrement()));
        }
        orders.put(order.getId(), order);
        return order;
    }

    public Order findById(String id) {
        return orders.get(id);
    }

    public List<Order> findByUserId(Long userId) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getUserId().equals(userId)) {
                result.add(order);
            }
        }
        return result;
    }

    public List<Order> findRecentByUserId(Long userId, int limit) {
        List<Order> userOrders = findByUserId(userId);
        // 简单返回所有订单，实际应该按时间排序并限制数量
        return userOrders.size() > limit ? userOrders.subList(0, limit) : userOrders;
    }
}

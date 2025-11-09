package com.example.order.repository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Order repository for data access
 */
@Repository
public class OrderRepository {

    public List<String> findByUserId(Long userId) {
        // Simulate database query
        List<String> orders = new ArrayList<>();
        orders.add("ORD-001");
        orders.add("ORD-002");
        return orders;
    }

    public void save(String orderId, Long userId, String productId) {
        // Simulate save
        System.out.println("Saving order: " + orderId);
    }

    public void delete(String orderId) {
        // Simulate delete
        System.out.println("Deleting order: " + orderId);
    }
}

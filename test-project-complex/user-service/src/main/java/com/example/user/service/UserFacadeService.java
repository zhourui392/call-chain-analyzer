package com.example.user.service;

import com.example.user.model.User;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFacadeService {

    @DubboReference(version = "1.0.0", group = "ecommerce")
    private com.example.product.api.ProductService productService;

    @DubboReference(version = "1.0.0", group = "ecommerce")
    private com.example.order.api.OrderService orderService;

    @DubboReference(version = "1.0.0", group = "ecommerce")
    private com.example.product.api.RecommendationService recommendationService;

    public String placeOrder(Long userId, String productId, Integer quantity, String address) {
        // 1. 检查库存
        boolean hasStock = productService.checkInventory(productId, quantity);
        if (!hasStock) {
            throw new RuntimeException("Insufficient inventory");
        }

        // 2. 创建订单（内部会调用支付和通知服务）
        com.example.order.model.Order order = orderService.createOrder(userId, productId, quantity, address);

        // 3. 扣减库存
        productService.decreaseInventory(productId, quantity);

        return order.getId();
    }

    public List<com.example.product.model.Product> getRecommendations(Long userId) {
        // 1. 获取用户订单历史
        List<String> orderHistory = orderService.getUserOrderHistory(userId);

        // 2. 基于订单历史推荐商品
        return recommendationService.recommend(userId, orderHistory);
    }
}

package com.example.order.service;

import com.example.order.api.OrderService;
import com.example.order.model.Order;
import com.example.order.repository.OrderRepository;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@DubboService(version = "1.0.0", group = "ecommerce")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @DubboReference(version = "1.0.0", group = "ecommerce")
    private com.example.user.api.UserInfoService userInfoService;

    @DubboReference(version = "1.0.0", group = "ecommerce")
    private com.example.product.api.ProductService productService;

    @DubboReference(version = "1.0.0", group = "ecommerce")
    private com.example.payment.api.PaymentService paymentService;

    @Override
    public Order createOrder(Long userId, String productId, Integer quantity, String address) {
        // 1. 获取用户信息
        com.example.user.model.User user = userInfoService.getUserInfo(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // 2. 获取商品信息并计算金额
        com.example.product.model.Product product = productService.getProductInfo(productId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        Double totalAmount = product.getPrice() * quantity;

        // 3. 保存订单
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setAddress(address);
        order.setStatus("PENDING");
        order = orderRepository.save(order);

        // 4. 创建支付订单
        String paymentId = paymentService.createPayment(order.getId(), totalAmount);

        return order;
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<String> getUserOrderHistory(Long userId) {
        List<Order> orders = orderRepository.findRecentByUserId(userId, 10);

        // 提取商品分类信息（这里简化为返回商品ID，实际应该查询商品获取分类）
        List<String> categories = new ArrayList<>();
        for (Order order : orders) {
            com.example.product.model.Product product = productService.getProductInfo(order.getProductId());
            if (product != null && product.getCategory() != null) {
                if (!categories.contains(product.getCategory())) {
                    categories.add(product.getCategory());
                }
            }
        }

        return categories;
    }
}

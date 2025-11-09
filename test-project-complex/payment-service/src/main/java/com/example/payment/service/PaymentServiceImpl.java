package com.example.payment.service;

import com.example.payment.api.PaymentService;
import com.example.payment.model.Payment;
import com.example.payment.repository.PaymentRepository;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService(version = "1.0.0", group = "ecommerce")
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @DubboReference(version = "1.0.0", group = "ecommerce")
    private com.example.notification.api.NotificationService notificationService;

    @Override
    public String createPayment(String orderId, Double amount) {
        // 创建支付记录
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus("PENDING");
        payment.setMethod("ALIPAY");
        payment = paymentRepository.save(payment);

        // 发送支付通知
        notificationService.sendNotification(
            extractUserIdFromOrder(orderId),
            "EMAIL",
            "您的订单 " + orderId + " 支付金额 " + amount + " 元，请尽快完成支付。"
        );

        return payment.getId();
    }

    @Override
    public String getPaymentStatus(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId);
        return payment != null ? payment.getStatus() : null;
    }

    private Long extractUserIdFromOrder(String orderId) {
        // 简单的用户ID提取逻辑，实际应该从订单服务获取
        return 123L;
    }
}

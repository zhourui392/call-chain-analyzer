package com.example.payment.api;

/**
 * 支付服务 Dubbo 接口
 */
public interface PaymentService {

    /**
     * 创建支付订单
     * @param orderId 订单ID
     * @param amount 支付金额
     * @return 支付ID
     */
    String createPayment(String orderId, Double amount);

    /**
     * 查询支付状态
     * @param paymentId 支付ID
     * @return 支付状态
     */
    String getPaymentStatus(String paymentId);
}

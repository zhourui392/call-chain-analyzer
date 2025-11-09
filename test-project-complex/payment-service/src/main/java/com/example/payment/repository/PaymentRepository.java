package com.example.payment.repository;

import com.example.payment.model.Payment;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class PaymentRepository {
    private final Map<String, Payment> payments = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            payment.setId("PAY" + String.format("%03d", idGenerator.getAndIncrement()));
        }
        payments.put(payment.getId(), payment);
        return payment;
    }

    public Payment findById(String id) {
        return payments.get(id);
    }

    public Payment findByOrderId(String orderId) {
        for (Payment payment : payments.values()) {
            if (payment.getOrderId().equals(orderId)) {
                return payment;
            }
        }
        return null;
    }
}

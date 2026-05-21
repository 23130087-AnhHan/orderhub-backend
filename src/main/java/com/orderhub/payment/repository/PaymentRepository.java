package com.orderhub.payment.repository;

import com.orderhub.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderIdAndOrderUserId(Long orderId, Long userId);

    boolean existsByOrderId(Long orderId);

    boolean existsByTransactionCode(String transactionCode);
}
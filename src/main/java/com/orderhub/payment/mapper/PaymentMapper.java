package com.orderhub.payment.mapper;

import com.orderhub.payment.dto.PaymentResponse;
import com.orderhub.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getOrder().getOrderCode(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionCode(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
package com.orderhub.event.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class PaymentEventMessage implements Serializable {

    private Long userId;
    private Long orderId;
    private String orderCode;
    private Long paymentId;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;

    public PaymentEventMessage() {
    }

    public PaymentEventMessage(
            Long userId,
            Long orderId,
            String orderCode,
            Long paymentId,
            BigDecimal amount,
            String paymentMethod,
            String paymentStatus
    ) {
        this.userId = userId;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
package com.orderhub.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private String orderCode;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionCode;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public PaymentResponse() {
    }

    public PaymentResponse(
            Long id,
            Long orderId,
            String orderCode,
            BigDecimal amount,
            String paymentMethod,
            String status,
            String transactionCode,
            LocalDateTime paidAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionCode = transactionCode;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
package com.orderhub.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSummaryResponse {

    private Long id;
    private String orderCode;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public OrderSummaryResponse() {
    }

    public OrderSummaryResponse(
            Long id,
            String orderCode,
            BigDecimal totalAmount,
            String status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.orderCode = orderCode;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
package com.orderhub.event.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderEventMessage implements Serializable {

    private Long userId;
    private Long orderId;
    private String orderCode;
    private BigDecimal totalAmount;

    public OrderEventMessage() {
    }

    public OrderEventMessage(Long userId, Long orderId, String orderCode, BigDecimal totalAmount) {
        this.userId = userId;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.totalAmount = totalAmount;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
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

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
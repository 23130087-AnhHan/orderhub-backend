package com.orderhub.order.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long id;
    private Long productVariantId;
    private String productName;
    private String variantInfo;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public OrderItemResponse() {
    }

    public OrderItemResponse(
            Long id,
            Long productVariantId,
            String productName,
            String variantInfo,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {
        this.id = id;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.variantInfo = variantInfo;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public Long getId() {
        return id;
    }

    public Long getProductVariantId() {
        return productVariantId;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariantInfo() {
        return variantInfo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProductVariantId(Long productVariantId) {
        this.productVariantId = productVariantId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setVariantInfo(String variantInfo) {
        this.variantInfo = variantInfo;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
package com.orderhub.product.dto;

import java.math.BigDecimal;

public class ProductVariantResponse {

    private Long id;
    private String sku;
    private String color;
    private String size;
    private Integer stockQuantity;
    private BigDecimal price;
    private String status;

    public ProductVariantResponse() {
    }

    public ProductVariantResponse(Long id, String sku, String color, String size, Integer stockQuantity, BigDecimal price, String status) {
        this.id = id;
        this.sku = sku;
        this.color = color;
        this.size = size;
        this.stockQuantity = stockQuantity;
        this.price = price;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getColor() {
        return color;
    }

    public String getSize() {
        return size;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
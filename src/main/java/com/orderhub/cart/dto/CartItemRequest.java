package com.orderhub.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartItemRequest {

    @NotNull(message = "Product variant ID is required")
    private Long productVariantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    public Long getProductVariantId() {
        return productVariantId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setProductVariantId(Long productVariantId) {
        this.productVariantId = productVariantId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
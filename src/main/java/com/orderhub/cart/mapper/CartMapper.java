package com.orderhub.cart.mapper;

import com.orderhub.cart.dto.CartItemResponse;
import com.orderhub.cart.dto.CartResponse;
import com.orderhub.cart.entity.Cart;
import com.orderhub.cart.entity.CartItem;
import com.orderhub.product.entity.Product;
import com.orderhub.product.entity.ProductVariant;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal totalAmount = itemResponses
                .stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                itemResponses,
                totalAmount
        );
    }

    public CartItemResponse toItemResponse(CartItem cartItem) {
        ProductVariant variant = cartItem.getProductVariant();
        Product product = variant.getProduct();

        BigDecimal unitPrice = variant.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                variant.getId(),
                product.getName(),
                product.getThumbnailUrl(),
                variant.getSku(),
                variant.getColor(),
                variant.getSize(),
                cartItem.getQuantity(),
                unitPrice,
                totalPrice
        );
    }
}
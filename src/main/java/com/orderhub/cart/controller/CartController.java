package com.orderhub.cart.controller;

import com.orderhub.cart.dto.CartItemRequest;
import com.orderhub.cart.dto.CartResponse;
import com.orderhub.cart.dto.UpdateCartItemRequest;
import com.orderhub.cart.service.CartService;
import com.orderhub.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResponse<CartResponse> getCurrentUserCart(Authentication authentication) {
        CartResponse response = cartService.getCurrentUserCart(authentication.getName());
        return ApiResponse.success("Cart retrieved successfully", response);
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItemToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request
    ) {
        CartResponse response = cartService.addItemToCart(authentication.getName(), request);
        return ApiResponse.success("Item added to cart successfully", response);
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartResponse> updateCartItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse response = cartService.updateCartItem(authentication.getName(), id, request);
        return ApiResponse.success("Cart item updated successfully", response);
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<CartResponse> removeCartItem(
            Authentication authentication,
            @PathVariable Long id
    ) {
        CartResponse response = cartService.removeCartItem(authentication.getName(), id);
        return ApiResponse.success("Cart item removed successfully", response);
    }
}
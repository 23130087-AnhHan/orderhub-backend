package com.orderhub.order.controller;

import com.orderhub.common.response.ApiResponse;
import com.orderhub.common.response.PageResponse;
import com.orderhub.order.dto.CreateOrderRequest;
import com.orderhub.order.dto.OrderFilterRequest;
import com.orderhub.order.dto.OrderResponse;
import com.orderhub.order.dto.OrderSummaryResponse;
import com.orderhub.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(authentication.getName(), request);
        return ApiResponse.success("Order created successfully", response);
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderSummaryResponse>> getMyOrders(
            Authentication authentication,
            OrderFilterRequest request
    ) {
        PageResponse<OrderSummaryResponse> response = orderService.getMyOrders(authentication.getName(), request);
        return ApiResponse.success("Orders retrieved successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getMyOrderDetail(
            Authentication authentication,
            @PathVariable Long id
    ) {
        OrderResponse response = orderService.getMyOrderDetail(authentication.getName(), id);
        return ApiResponse.success("Order retrieved successfully", response);
    }
}
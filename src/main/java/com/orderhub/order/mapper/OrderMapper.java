package com.orderhub.order.mapper;

import com.orderhub.order.dto.OrderItemResponse;
import com.orderhub.order.dto.OrderResponse;
import com.orderhub.order.dto.OrderSummaryResponse;
import com.orderhub.order.entity.Order;
import com.orderhub.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getNote(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public OrderSummaryResponse toSummaryResponse(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderCode(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductVariant().getId(),
                item.getProductName(),
                item.getVariantInfo(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}
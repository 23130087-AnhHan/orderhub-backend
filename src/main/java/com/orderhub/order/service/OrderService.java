package com.orderhub.order.service;

import com.orderhub.cart.entity.Cart;
import com.orderhub.cart.entity.CartItem;
import com.orderhub.cart.repository.CartRepository;
import com.orderhub.common.exception.BusinessException;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.common.response.PageResponse;
import com.orderhub.order.dto.CreateOrderRequest;
import com.orderhub.order.dto.OrderFilterRequest;
import com.orderhub.order.dto.OrderResponse;
import com.orderhub.order.dto.OrderSummaryResponse;
import com.orderhub.order.entity.Order;
import com.orderhub.order.entity.OrderItem;
import com.orderhub.order.mapper.OrderMapper;
import com.orderhub.order.repository.OrderRepository;
import com.orderhub.product.entity.Product;
import com.orderhub.product.entity.ProductVariant;
import com.orderhub.product.repository.ProductVariantRepository;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            ProductVariantRepository productVariantRepository,
            UserRepository userRepository,
            OrderMapper orderMapper
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = getUserByEmail(email);

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        validateStock(cart);

        Order order = new Order();
        order.setUser(user);
        order.setOrderCode(generateOrderCode());
        order.setStatus("PENDING");
        order.setNote(request.getNote());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getProductVariant();
            Product product = variant.getProduct();

            BigDecimal unitPrice = variant.getPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProductVariant(variant);
            orderItem.setProductName(product.getName());
            orderItem.setVariantInfo(buildVariantInfo(variant));
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(totalPrice);

            order.addItem(orderItem);

            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            totalAmount = totalAmount.add(totalPrice);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toResponse(savedOrder);
    }

    public PageResponse<OrderSummaryResponse> getMyOrders(String email, OrderFilterRequest request) {
        User user = getUserByEmail(email);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<Order> orderPage;

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            orderPage = orderRepository.findByUserIdAndStatus(user.getId(), request.getStatus(), pageable);
        } else {
            orderPage = orderRepository.findByUserId(user.getId(), pageable);
        }

        return PageResponse.of(
                orderPage.getContent()
                        .stream()
                        .map(orderMapper::toSummaryResponse)
                        .toList(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    public OrderResponse getMyOrderDetail(String email, Long orderId) {
        User user = getUserByEmail(email);

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return orderMapper.toResponse(order);
    }

    private void validateStock(Cart cart) {
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getProductVariant();

            if (!"ACTIVE".equals(variant.getStatus())) {
                throw new BusinessException("Product variant is not active: " + variant.getSku());
            }

            if (cartItem.getQuantity() > variant.getStockQuantity()) {
                throw new BusinessException("Not enough stock for SKU: " + variant.getSku());
            }
        }
    }

    private String buildVariantInfo(ProductVariant variant) {
        String color = variant.getColor() == null ? "" : variant.getColor();
        String size = variant.getSize() == null ? "" : variant.getSize();

        if (!color.isBlank() && !size.isBlank()) {
            return color + " / " + size;
        }

        if (!color.isBlank()) {
            return color;
        }

        if (!size.isBlank()) {
            return size;
        }

        return variant.getSku();
    }

    private String generateOrderCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return "ORD-" + date + "-" + random;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
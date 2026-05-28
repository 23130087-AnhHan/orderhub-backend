package com.orderhub.order.service;

import com.orderhub.cart.entity.Cart;
import com.orderhub.cart.entity.CartItem;
import com.orderhub.cart.repository.CartRepository;
import com.orderhub.common.exception.BusinessException;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.common.response.PageResponse;
import com.orderhub.event.dto.OrderEventMessage;
import com.orderhub.event.publisher.EventPublisher;
import com.orderhub.order.dto.CreateOrderRequest;
import com.orderhub.order.dto.OrderFilterRequest;
import com.orderhub.order.dto.OrderResponse;
import com.orderhub.order.dto.OrderSummaryResponse;
import com.orderhub.order.entity.Order;
import com.orderhub.order.mapper.OrderMapper;
import com.orderhub.order.repository.OrderRepository;
import com.orderhub.product.entity.Product;
import com.orderhub.product.entity.ProductVariant;
import com.orderhub.product.repository.ProductVariantRepository;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;
    private ProductVariant productVariant;
    private CartItem cartItem;
    private CreateOrderRequest createOrderRequest;
    private Order savedOrder;
    private OrderResponse orderResponse;
    private OrderSummaryResponse orderSummaryResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFullName("John Doe");

        product = new Product();
        product.setId(1L);
        product.setName("Basic T-Shirt");
        product.setThumbnailUrl("https://example.com/tshirt.jpg");
        product.setStatus("ACTIVE");

        productVariant = new ProductVariant();
        productVariant.setId(1L);
        productVariant.setProduct(product);
        productVariant.setSku("TSHIRT-WHITE-M-001");
        productVariant.setColor("White");
        productVariant.setSize("M");
        productVariant.setStockQuantity(10);
        productVariant.setPrice(new BigDecimal("199000"));
        productVariant.setStatus("ACTIVE");

        cart = new Cart(user);
        cart.setId(1L);

        cartItem = new CartItem(cart, productVariant, 2);
        cartItem.setId(1L);
        cart.addItem(cartItem);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setNote("Please deliver quickly");

        savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUser(user);
        savedOrder.setOrderCode("ORD-20260528-ABC12345");
        savedOrder.setStatus("PENDING");
        savedOrder.setNote("Please deliver quickly");
        savedOrder.setTotalAmount(new BigDecimal("398000"));

        orderResponse = new OrderResponse();
        orderResponse.setId(1L);
        orderResponse.setOrderCode("ORD-20260528-ABC12345");
        orderResponse.setStatus("PENDING");
        orderResponse.setNote("Please deliver quickly");
        orderResponse.setTotalAmount(new BigDecimal("398000"));
        orderResponse.setItems(List.of());

        orderSummaryResponse = new OrderSummaryResponse();
        orderSummaryResponse.setId(1L);
        orderSummaryResponse.setOrderCode("ORD-20260528-ABC12345");
        orderSummaryResponse.setStatus("PENDING");
        orderSummaryResponse.setTotalAmount(new BigDecimal("398000"));
    }

    @Test
    void createOrder_ShouldReturnOrderResponse_WhenCartIsValid() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    order.setOrderCode("ORD-20260528-ABC12345");
                    return order;
                });

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(orderResponse);

        // Act
        OrderResponse response = orderService.createOrder("john@example.com", createOrderRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PENDING", response.getStatus());
        assertEquals(new BigDecimal("398000"), response.getTotalAmount());
        assertEquals("Please deliver quickly", response.getNote());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(productVariantRepository).save(productVariant);
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishOrderCreated(any(OrderEventMessage.class));
        verify(cartRepository).save(cart);
        verify(orderMapper).toResponse(any(Order.class));
    }

    @Test
    void createOrder_ShouldCreateOrderWithCorrectSnapshotData_WhenCartIsValid() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(orderResponse);

        // Act
        orderService.createOrder("john@example.com", createOrderRequest);

        // Assert
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order orderToSave = orderCaptor.getValue();

        assertEquals(user, orderToSave.getUser());
        assertEquals("PENDING", orderToSave.getStatus());
        assertEquals("Please deliver quickly", orderToSave.getNote());
        assertEquals(new BigDecimal("398000"), orderToSave.getTotalAmount());
        assertTrue(orderToSave.getOrderCode().startsWith("ORD-"));

        assertEquals(1, orderToSave.getItems().size());
        assertEquals(productVariant, orderToSave.getItems().get(0).getProductVariant());
        assertEquals("Basic T-Shirt", orderToSave.getItems().get(0).getProductName());
        assertEquals("White / M", orderToSave.getItems().get(0).getVariantInfo());
        assertEquals(2, orderToSave.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("199000"), orderToSave.getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("398000"), orderToSave.getItems().get(0).getTotalPrice());
    }

    @Test
    void createOrder_ShouldDecreaseStock_WhenOrderCreatedSuccessfully() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(orderResponse);

        // Act
        orderService.createOrder("john@example.com", createOrderRequest);

        // Assert
        assertEquals(8, productVariant.getStockQuantity());
        verify(productVariantRepository).save(productVariant);
    }

    @Test
    void createOrder_ShouldClearCart_WhenOrderCreatedSuccessfully() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(orderResponse);

        // Act
        orderService.createOrder("john@example.com", createOrderRequest);

        // Assert
        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void createOrder_ShouldPublishOrderCreatedEvent_WhenOrderCreatedSuccessfully() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    order.setOrderCode("ORD-20260528-ABC12345");
                    return order;
                });

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(orderResponse);

        // Act
        orderService.createOrder("john@example.com", createOrderRequest);

        // Assert
        ArgumentCaptor<OrderEventMessage> eventCaptor = ArgumentCaptor.forClass(OrderEventMessage.class);
        verify(eventPublisher).publishOrderCreated(eventCaptor.capture());

        OrderEventMessage eventMessage = eventCaptor.getValue();

        assertEquals(1L, eventMessage.getUserId());
        assertEquals(1L, eventMessage.getOrderId());
        assertEquals("ORD-20260528-ABC12345", eventMessage.getOrderCode());
        assertEquals(new BigDecimal("398000"), eventMessage.getTotalAmount());
    }

    @Test
    void createOrder_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder("unknown@example.com", createOrderRequest)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("unknown@example.com");
        verify(cartRepository, never()).findByUserId(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishOrderCreated(any(OrderEventMessage.class));
    }

    @Test
    void createOrder_ShouldThrowBusinessException_WhenCartDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder("john@example.com", createOrderRequest)
        );

        assertEquals("Cart is empty", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishOrderCreated(any(OrderEventMessage.class));
    }

    @Test
    void createOrder_ShouldThrowBusinessException_WhenCartIsEmpty() {
        // Arrange
        Cart emptyCart = new Cart(user);
        emptyCart.setId(2L);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(emptyCart));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder("john@example.com", createOrderRequest)
        );

        assertEquals("Cart is empty", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(orderRepository, never()).save(any(Order.class));
        verify(productVariantRepository, never()).save(any(ProductVariant.class));
        verify(eventPublisher, never()).publishOrderCreated(any(OrderEventMessage.class));
    }

    @Test
    void createOrder_ShouldThrowBusinessException_WhenProductVariantIsNotActive() {
        // Arrange
        productVariant.setStatus("INACTIVE");

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder("john@example.com", createOrderRequest)
        );

        assertEquals("Product variant is not active: TSHIRT-WHITE-M-001", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(orderRepository, never()).save(any(Order.class));
        verify(productVariantRepository, never()).save(any(ProductVariant.class));
        verify(eventPublisher, never()).publishOrderCreated(any(OrderEventMessage.class));
    }

    @Test
    void createOrder_ShouldThrowBusinessException_WhenStockIsNotEnough() {
        // Arrange
        productVariant.setStockQuantity(1);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder("john@example.com", createOrderRequest)
        );

        assertEquals("Not enough stock for SKU: TSHIRT-WHITE-M-001", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(orderRepository, never()).save(any(Order.class));
        verify(productVariantRepository, never()).save(any(ProductVariant.class));
        verify(eventPublisher, never()).publishOrderCreated(any(OrderEventMessage.class));
    }

    @Test
    void getMyOrders_ShouldReturnPageResponse_WhenStatusIsProvided() {
        // Arrange
        OrderFilterRequest filterRequest = new OrderFilterRequest();
        filterRequest.setPage(0);
        filterRequest.setSize(10);
        filterRequest.setStatus("PENDING");

        Page<Order> orderPage = new PageImpl<>(List.of(savedOrder));

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByUserIdAndStatus(eq(user.getId()), eq("PENDING"), any(Pageable.class)))
                .thenReturn(orderPage);

        when(orderMapper.toSummaryResponse(savedOrder))
                .thenReturn(orderSummaryResponse);

        // Act
        PageResponse<OrderSummaryResponse> response = orderService.getMyOrders("john@example.com", filterRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals("PENDING", response.getItems().get(0).getStatus());
        assertEquals(new BigDecimal("398000"), response.getItems().get(0).getTotalAmount());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByUserIdAndStatus(eq(user.getId()), eq("PENDING"), any(Pageable.class));
        verify(orderRepository, never()).findByUserId(anyLong(), any(Pageable.class));
        verify(orderMapper).toSummaryResponse(savedOrder);
    }

    @Test
    void getMyOrders_ShouldReturnPageResponse_WhenStatusIsNotProvided() {
        // Arrange
        OrderFilterRequest filterRequest = new OrderFilterRequest();
        filterRequest.setPage(0);
        filterRequest.setSize(10);
        filterRequest.setStatus(null);

        Page<Order> orderPage = new PageImpl<>(List.of(savedOrder));

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByUserId(eq(user.getId()), any(Pageable.class)))
                .thenReturn(orderPage);

        when(orderMapper.toSummaryResponse(savedOrder))
                .thenReturn(orderSummaryResponse);

        // Act
        PageResponse<OrderSummaryResponse> response = orderService.getMyOrders("john@example.com", filterRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals("ORD-20260528-ABC12345", response.getItems().get(0).getOrderCode());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByUserId(eq(user.getId()), any(Pageable.class));
        verify(orderRepository, never()).findByUserIdAndStatus(anyLong(), anyString(), any(Pageable.class));
        verify(orderMapper).toSummaryResponse(savedOrder);
    }

    @Test
    void getMyOrderDetail_ShouldReturnOrderResponse_WhenOrderBelongsToUser() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(1L, user.getId()))
                .thenReturn(Optional.of(savedOrder));

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(orderResponse);

        // Act
        OrderResponse response = orderService.getMyOrderDetail("john@example.com", 1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ORD-20260528-ABC12345", response.getOrderCode());
        assertEquals("PENDING", response.getStatus());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByIdAndUserId(1L, user.getId());
        verify(orderMapper).toResponse(savedOrder);
    }

    @Test
    void getMyOrderDetail_ShouldThrowResourceNotFoundException_WhenOrderDoesNotBelongToUser() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(999L, user.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getMyOrderDetail("john@example.com", 999L)
        );

        assertEquals("Order not found", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByIdAndUserId(999L, user.getId());
        verify(orderMapper, never()).toResponse(any(Order.class));
    }
}
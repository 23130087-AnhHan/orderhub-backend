package com.orderhub.payment.service;

import com.orderhub.common.exception.BusinessException;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.event.dto.PaymentEventMessage;
import com.orderhub.event.publisher.EventPublisher;
import com.orderhub.order.entity.Order;
import com.orderhub.order.repository.OrderRepository;
import com.orderhub.payment.dto.FakePaymentRequest;
import com.orderhub.payment.dto.PaymentResponse;
import com.orderhub.payment.entity.Payment;
import com.orderhub.payment.mapper.PaymentMapper;
import com.orderhub.payment.repository.PaymentRepository;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private Order order;
    private Payment payment;
    private FakePaymentRequest successRequest;
    private FakePaymentRequest failedRequest;
    private PaymentResponse successPaymentResponse;
    private PaymentResponse failedPaymentResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFullName("John Doe");

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setOrderCode("ORD-20260528-ABC12345");
        order.setStatus("PENDING");
        order.setTotalAmount(new BigDecimal("398000"));

        payment = new Payment();
        payment.setId(1L);
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("398000"));
        payment.setPaymentMethod("FAKE_PAYMENT");
        payment.setStatus("SUCCESS");
        payment.setTransactionCode("PAY-ABC12345");

        successRequest = new FakePaymentRequest();
        successRequest.setOrderId(1L);
        successRequest.setPaymentMethod("FAKE_PAYMENT");
        successRequest.setSuccess(true);

        failedRequest = new FakePaymentRequest();
        failedRequest.setOrderId(1L);
        failedRequest.setPaymentMethod("FAKE_PAYMENT");
        failedRequest.setSuccess(false);

        successPaymentResponse = new PaymentResponse();
        successPaymentResponse.setId(1L);
        successPaymentResponse.setOrderId(1L);
        successPaymentResponse.setOrderCode("ORD-20260528-ABC12345");
        successPaymentResponse.setAmount(new BigDecimal("398000"));
        successPaymentResponse.setPaymentMethod("FAKE_PAYMENT");
        successPaymentResponse.setStatus("SUCCESS");
        successPaymentResponse.setTransactionCode("PAY-ABC12345");

        failedPaymentResponse = new PaymentResponse();
        failedPaymentResponse.setId(1L);
        failedPaymentResponse.setOrderId(1L);
        failedPaymentResponse.setOrderCode("ORD-20260528-ABC12345");
        failedPaymentResponse.setAmount(new BigDecimal("398000"));
        failedPaymentResponse.setPaymentMethod("FAKE_PAYMENT");
        failedPaymentResponse.setStatus("FAILED");
        failedPaymentResponse.setTransactionCode("PAY-ABC12345");
    }

    @Test
    void fakePay_ShouldReturnSuccessPaymentResponse_WhenPaymentIsSuccessful() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(successRequest.getOrderId(), user.getId()))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(order.getId()))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment savedPayment = invocation.getArgument(0);
                    savedPayment.setId(1L);
                    savedPayment.setTransactionCode("PAY-ABC12345");
                    return savedPayment;
                });

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(successPaymentResponse);

        // Act
        PaymentResponse response = paymentService.fakePay("john@example.com", successRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getOrderId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("FAKE_PAYMENT", response.getPaymentMethod());
        assertEquals(new BigDecimal("398000"), response.getAmount());

        assertEquals("PAID", order.getStatus());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByIdAndUserId(successRequest.getOrderId(), user.getId());
        verify(paymentRepository).existsByOrderId(order.getId());
        verify(orderRepository).save(order);
        verify(paymentRepository).save(any(Payment.class));
        verify(eventPublisher).publishPaymentSuccess(any(PaymentEventMessage.class));
        verify(eventPublisher, never()).publishPaymentFailed(any(PaymentEventMessage.class));
        verify(paymentMapper).toResponse(any(Payment.class));
    }

    @Test
    void fakePay_ShouldCreateSuccessPaymentWithCorrectData_WhenPaymentIsSuccessful() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(successRequest.getOrderId(), user.getId()))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(order.getId()))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment savedPayment = invocation.getArgument(0);
                    savedPayment.setId(1L);
                    return savedPayment;
                });

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(successPaymentResponse);

        // Act
        paymentService.fakePay("john@example.com", successRequest);

        // Assert
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();

        assertEquals(order, savedPayment.getOrder());
        assertEquals("FAKE_PAYMENT", savedPayment.getPaymentMethod());
        assertEquals(new BigDecimal("398000"), savedPayment.getAmount());
        assertEquals("SUCCESS", savedPayment.getStatus());
        assertNotNull(savedPayment.getPaidAt());
        assertNotNull(savedPayment.getTransactionCode());
        assertTrue(savedPayment.getTransactionCode().startsWith("PAY-"));
    }

    @Test
    void fakePay_ShouldPublishPaymentSuccessEvent_WhenPaymentIsSuccessful() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(successRequest.getOrderId(), user.getId()))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(order.getId()))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment savedPayment = invocation.getArgument(0);
                    savedPayment.setId(1L);
                    savedPayment.setTransactionCode("PAY-ABC12345");
                    return savedPayment;
                });

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(successPaymentResponse);

        // Act
        paymentService.fakePay("john@example.com", successRequest);

        // Assert
        ArgumentCaptor<PaymentEventMessage> eventCaptor = ArgumentCaptor.forClass(PaymentEventMessage.class);
        verify(eventPublisher).publishPaymentSuccess(eventCaptor.capture());

        PaymentEventMessage eventMessage = eventCaptor.getValue();

        assertEquals(1L, eventMessage.getUserId());
        assertEquals(1L, eventMessage.getOrderId());
        assertEquals("ORD-20260528-ABC12345", eventMessage.getOrderCode());
        assertEquals(1L, eventMessage.getPaymentId());
        assertEquals(new BigDecimal("398000"), eventMessage.getAmount());
        assertEquals("FAKE_PAYMENT", eventMessage.getPaymentMethod());
        assertEquals("SUCCESS", eventMessage.getPaymentStatus());
    }

    @Test
    void fakePay_ShouldReturnFailedPaymentResponse_WhenPaymentIsFailed() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(failedRequest.getOrderId(), user.getId()))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(order.getId()))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment savedPayment = invocation.getArgument(0);
                    savedPayment.setId(1L);
                    savedPayment.setTransactionCode("PAY-ABC12345");
                    return savedPayment;
                });

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(failedPaymentResponse);

        // Act
        PaymentResponse response = paymentService.fakePay("john@example.com", failedRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("FAILED", response.getStatus());
        assertEquals("PENDING", order.getStatus());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByIdAndUserId(failedRequest.getOrderId(), user.getId());
        verify(paymentRepository).existsByOrderId(order.getId());
        verify(orderRepository, never()).save(order);
        verify(paymentRepository).save(any(Payment.class));
        verify(eventPublisher).publishPaymentFailed(any(PaymentEventMessage.class));
        verify(eventPublisher, never()).publishPaymentSuccess(any(PaymentEventMessage.class));
        verify(paymentMapper).toResponse(any(Payment.class));
    }

    @Test
    void fakePay_ShouldCreateFailedPaymentWithCorrectData_WhenPaymentIsFailed() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(failedRequest.getOrderId(), user.getId()))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(order.getId()))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment savedPayment = invocation.getArgument(0);
                    savedPayment.setId(1L);
                    return savedPayment;
                });

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(failedPaymentResponse);

        // Act
        paymentService.fakePay("john@example.com", failedRequest);

        // Assert
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();

        assertEquals(order, savedPayment.getOrder());
        assertEquals("FAKE_PAYMENT", savedPayment.getPaymentMethod());
        assertEquals(new BigDecimal("398000"), savedPayment.getAmount());
        assertEquals("FAILED", savedPayment.getStatus());
        assertNull(savedPayment.getPaidAt());
        assertNotNull(savedPayment.getTransactionCode());
        assertTrue(savedPayment.getTransactionCode().startsWith("PAY-"));
    }

    @Test
    void fakePay_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.fakePay("unknown@example.com", successRequest)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("unknown@example.com");
        verify(orderRepository, never()).findByIdAndUserId(anyLong(), anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publishPaymentSuccess(any(PaymentEventMessage.class));
        verify(eventPublisher, never()).publishPaymentFailed(any(PaymentEventMessage.class));
    }

    @Test
    void fakePay_ShouldThrowResourceNotFoundException_WhenOrderDoesNotBelongToUser() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(successRequest.getOrderId(), user.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.fakePay("john@example.com", successRequest)
        );

        assertEquals("Order not found", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByIdAndUserId(successRequest.getOrderId(), user.getId());
        verify(paymentRepository, never()).existsByOrderId(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publishPaymentSuccess(any(PaymentEventMessage.class));
        verify(eventPublisher, never()).publishPaymentFailed(any(PaymentEventMessage.class));
    }

    @Test
    void fakePay_ShouldThrowBusinessException_WhenOrderAlreadyPaid() {
        // Arrange
        order.setStatus("PAID");

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(successRequest.getOrderId(), user.getId()))
                .thenReturn(Optional.of(order));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.fakePay("john@example.com", successRequest)
        );

        assertEquals("Order has already been paid", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByIdAndUserId(successRequest.getOrderId(), user.getId());
        verify(paymentRepository, never()).existsByOrderId(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publishPaymentSuccess(any(PaymentEventMessage.class));
        verify(eventPublisher, never()).publishPaymentFailed(any(PaymentEventMessage.class));
    }

    @Test
    void fakePay_ShouldThrowBusinessException_WhenOrderIsCancelled() {
        // Arrange
        order.setStatus("CANCELLED");

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(successRequest.getOrderId(), user.getId()))
                .thenReturn(Optional.of(order));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.fakePay("john@example.com", successRequest)
        );

        assertEquals("Cancelled order cannot be paid", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByIdAndUserId(successRequest.getOrderId(), user.getId());
        verify(paymentRepository, never()).existsByOrderId(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publishPaymentSuccess(any(PaymentEventMessage.class));
        verify(eventPublisher, never()).publishPaymentFailed(any(PaymentEventMessage.class));
    }

    @Test
    void fakePay_ShouldThrowBusinessException_WhenPaymentAlreadyExistsForOrder() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(successRequest.getOrderId(), user.getId()))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(order.getId()))
                .thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.fakePay("john@example.com", successRequest)
        );

        assertEquals("Payment already exists for this order", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(orderRepository).findByIdAndUserId(successRequest.getOrderId(), user.getId());
        verify(paymentRepository).existsByOrderId(order.getId());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publishPaymentSuccess(any(PaymentEventMessage.class));
        verify(eventPublisher, never()).publishPaymentFailed(any(PaymentEventMessage.class));
    }

    @Test
    void getPaymentByOrder_ShouldReturnPaymentResponse_WhenPaymentExistsAndBelongsToUser() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(paymentRepository.findByOrderIdAndOrderUserId(1L, user.getId()))
                .thenReturn(Optional.of(payment));

        when(paymentMapper.toResponse(payment))
                .thenReturn(successPaymentResponse);

        // Act
        PaymentResponse response = paymentService.getPaymentByOrder("john@example.com", 1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getOrderId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("398000"), response.getAmount());

        verify(userRepository).findByEmail("john@example.com");
        verify(paymentRepository).findByOrderIdAndOrderUserId(1L, user.getId());
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    void getPaymentByOrder_ShouldThrowResourceNotFoundException_WhenPaymentDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(paymentRepository.findByOrderIdAndOrderUserId(999L, user.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getPaymentByOrder("john@example.com", 999L)
        );

        assertEquals("Payment not found", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(paymentRepository).findByOrderIdAndOrderUserId(999L, user.getId());
        verify(paymentMapper, never()).toResponse(any(Payment.class));
    }
}
package com.orderhub.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderhub.event.config.RabbitMQConfig;
import com.orderhub.event.dto.OrderEventMessage;
import com.orderhub.event.dto.PaymentEventMessage;
import com.orderhub.notification.service.NotificationService;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationEventConsumer notificationEventConsumer;

    private User user;
    private OrderEventMessage orderEventMessage;
    private PaymentEventMessage paymentSuccessEventMessage;
    private PaymentEventMessage paymentFailedEventMessage;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFullName("John Doe");

        orderEventMessage = new OrderEventMessage(
                1L,
                1L,
                "ORD-20260528-ABC12345",
                new BigDecimal("398000")
        );

        paymentSuccessEventMessage = new PaymentEventMessage(
                1L,
                1L,
                "ORD-20260528-ABC12345",
                1L,
                new BigDecimal("398000"),
                "FAKE_PAYMENT",
                "SUCCESS"
        );

        paymentFailedEventMessage = new PaymentEventMessage(
                1L,
                1L,
                "ORD-20260528-ABC12345",
                1L,
                new BigDecimal("398000"),
                "FAKE_PAYMENT",
                "FAILED"
        );
    }

    @Test
    void handleEvent_ShouldCreateOrderCreatedNotification_WhenRoutingKeyIsOrderCreated() throws Exception {
        // Arrange
        Message message = createMessage(RabbitMQConfig.ORDER_CREATED_ROUTING_KEY);

        when(objectMapper.readValue(any(byte[].class), eq(OrderEventMessage.class)))
                .thenReturn(orderEventMessage);

        when(userRepository.findById(orderEventMessage.getUserId()))
                .thenReturn(Optional.of(user));

        // Act
        notificationEventConsumer.handleEvent(message);

        // Assert
        verify(objectMapper).readValue(any(byte[].class), eq(OrderEventMessage.class));
        verify(userRepository).findById(orderEventMessage.getUserId());

        verify(notificationService).createNotification(
                user,
                "ORDER_CREATED",
                "Order created",
                "Your order ORD-20260528-ABC12345 has been created and is waiting for payment."
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void handleEvent_ShouldCreatePaymentSuccessNotification_WhenRoutingKeyIsPaymentSuccess() throws Exception {
        // Arrange
        Message message = createMessage(RabbitMQConfig.PAYMENT_SUCCESS_ROUTING_KEY);

        when(objectMapper.readValue(any(byte[].class), eq(PaymentEventMessage.class)))
                .thenReturn(paymentSuccessEventMessage);

        when(userRepository.findById(paymentSuccessEventMessage.getUserId()))
                .thenReturn(Optional.of(user));

        // Act
        notificationEventConsumer.handleEvent(message);

        // Assert
        verify(objectMapper).readValue(any(byte[].class), eq(PaymentEventMessage.class));
        verify(userRepository).findById(paymentSuccessEventMessage.getUserId());

        verify(notificationService).createNotification(
                user,
                "PAYMENT_SUCCESS",
                "Payment successful",
                "Your payment for order ORD-20260528-ABC12345 was successful."
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void handleEvent_ShouldCreatePaymentFailedNotification_WhenRoutingKeyIsPaymentFailed() throws Exception {
        // Arrange
        Message message = createMessage(RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY);

        when(objectMapper.readValue(any(byte[].class), eq(PaymentEventMessage.class)))
                .thenReturn(paymentFailedEventMessage);

        when(userRepository.findById(paymentFailedEventMessage.getUserId()))
                .thenReturn(Optional.of(user));

        // Act
        notificationEventConsumer.handleEvent(message);

        // Assert
        verify(objectMapper).readValue(any(byte[].class), eq(PaymentEventMessage.class));
        verify(userRepository).findById(paymentFailedEventMessage.getUserId());

        verify(notificationService).createNotification(
                user,
                "PAYMENT_FAILED",
                "Payment failed",
                "Your payment for order ORD-20260528-ABC12345 failed. Please try again."
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void handleEvent_ShouldDoNothing_WhenRoutingKeyIsUnknown() {
        // Arrange
        Message message = createMessage("unknown.routing.key");

        // Act
        notificationEventConsumer.handleEvent(message);

        // Assert
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(notificationService);
    }

    @Test
    void handleEvent_ShouldThrowRuntimeException_WhenOrderCreatedUserDoesNotExist() throws Exception {
        // Arrange
        Message message = createMessage(RabbitMQConfig.ORDER_CREATED_ROUTING_KEY);

        when(objectMapper.readValue(any(byte[].class), eq(OrderEventMessage.class)))
                .thenReturn(orderEventMessage);

        when(userRepository.findById(orderEventMessage.getUserId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                RuntimeException.class,
                () -> notificationEventConsumer.handleEvent(message)
        );

        verify(objectMapper).readValue(any(byte[].class), eq(OrderEventMessage.class));
        verify(userRepository).findById(orderEventMessage.getUserId());
        verifyNoInteractions(notificationService);
    }

    @Test
    void handleEvent_ShouldThrowRuntimeException_WhenPaymentEventUserDoesNotExist() throws Exception {
        // Arrange
        Message message = createMessage(RabbitMQConfig.PAYMENT_SUCCESS_ROUTING_KEY);

        when(objectMapper.readValue(any(byte[].class), eq(PaymentEventMessage.class)))
                .thenReturn(paymentSuccessEventMessage);

        when(userRepository.findById(paymentSuccessEventMessage.getUserId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                RuntimeException.class,
                () -> notificationEventConsumer.handleEvent(message)
        );

        verify(objectMapper).readValue(any(byte[].class), eq(PaymentEventMessage.class));
        verify(userRepository).findById(paymentSuccessEventMessage.getUserId());
        verifyNoInteractions(notificationService);
    }

    private Message createMessage(String routingKey) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setReceivedRoutingKey(routingKey);

        byte[] body = """
                {
                  "mock": "payload"
                }
                """.getBytes(StandardCharsets.UTF_8);

        return new Message(body, messageProperties);
    }
}
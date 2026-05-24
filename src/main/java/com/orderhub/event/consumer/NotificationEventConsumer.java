package com.orderhub.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderhub.event.config.RabbitMQConfig;
import com.orderhub.event.dto.OrderEventMessage;
import com.orderhub.event.dto.PaymentEventMessage;
import com.orderhub.notification.service.NotificationService;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(
            NotificationService notificationService,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleEvent(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        log.info("Received event from RabbitMQ. routingKey={}", routingKey);

        try {
            if (RabbitMQConfig.ORDER_CREATED_ROUTING_KEY.equals(routingKey)) {
                OrderEventMessage orderEventMessage = objectMapper.readValue(
                        message.getBody(),
                        OrderEventMessage.class
                );

                handleOrderCreated(orderEventMessage);
                return;
            }

            if (RabbitMQConfig.PAYMENT_SUCCESS_ROUTING_KEY.equals(routingKey)
                    || RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY.equals(routingKey)) {

                PaymentEventMessage paymentEventMessage = objectMapper.readValue(
                        message.getBody(),
                        PaymentEventMessage.class
                );

                handlePaymentEvent(paymentEventMessage);
                return;
            }

            log.warn("Unknown routing key: {}", routingKey);

        } catch (Exception e) {
            log.error("Failed to handle RabbitMQ event. routingKey={}", routingKey, e);
            throw new RuntimeException(e);
        }
    }

    private void handleOrderCreated(OrderEventMessage message) {
        log.info("Handling ORDER_CREATED event. orderCode={}, userId={}",
                message.getOrderCode(),
                message.getUserId()
        );

        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.createNotification(
                user,
                "ORDER_CREATED",
                "Order created",
                "Your order " + message.getOrderCode() + " has been created and is waiting for payment."
        );

        log.info("ORDER_CREATED notification saved. orderCode={}", message.getOrderCode());
    }

    private void handlePaymentEvent(PaymentEventMessage message) {
        log.info("Handling PAYMENT event. status={}, orderCode={}, userId={}",
                message.getPaymentStatus(),
                message.getOrderCode(),
                message.getUserId()
        );

        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("SUCCESS".equals(message.getPaymentStatus())) {
            notificationService.createNotification(
                    user,
                    "PAYMENT_SUCCESS",
                    "Payment successful",
                    "Your payment for order " + message.getOrderCode() + " was successful."
            );

            log.info("PAYMENT_SUCCESS notification saved. orderCode={}", message.getOrderCode());
            return;
        }

        if ("FAILED".equals(message.getPaymentStatus())) {
            notificationService.createNotification(
                    user,
                    "PAYMENT_FAILED",
                    "Payment failed",
                    "Your payment for order " + message.getOrderCode() + " failed. Please try again."
            );

            log.info("PAYMENT_FAILED notification saved. orderCode={}", message.getOrderCode());
        }
    }
}
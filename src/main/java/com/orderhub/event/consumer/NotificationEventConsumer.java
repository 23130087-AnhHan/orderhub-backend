package com.orderhub.event.consumer;

import com.orderhub.event.config.RabbitMQConfig;
import com.orderhub.event.dto.OrderEventMessage;
import com.orderhub.event.dto.PaymentEventMessage;
import com.orderhub.notification.service.NotificationService;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationEventConsumer(
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleEvent(Object message) {
        if (message instanceof OrderEventMessage orderEventMessage) {
            handleOrderCreated(orderEventMessage);
        }

        if (message instanceof PaymentEventMessage paymentEventMessage) {
            handlePaymentEvent(paymentEventMessage);
        }
    }

    private void handleOrderCreated(OrderEventMessage message) {
        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.createNotification(
                user,
                "ORDER_CREATED",
                "Order created",
                "Your order " + message.getOrderCode() + " has been created and is waiting for payment."
        );
    }

    private void handlePaymentEvent(PaymentEventMessage message) {
        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("SUCCESS".equals(message.getPaymentStatus())) {
            notificationService.createNotification(
                    user,
                    "PAYMENT_SUCCESS",
                    "Payment successful",
                    "Your payment for order " + message.getOrderCode() + " was successful."
            );
            return;
        }

        if ("FAILED".equals(message.getPaymentStatus())) {
            notificationService.createNotification(
                    user,
                    "PAYMENT_FAILED",
                    "Payment failed",
                    "Your payment for order " + message.getOrderCode() + " failed. Please try again."
            );
        }
    }
}
package com.orderhub.event.publisher;

import com.orderhub.event.config.RabbitMQConfig;
import com.orderhub.event.dto.OrderEventMessage;
import com.orderhub.event.dto.PaymentEventMessage;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final AmqpTemplate amqpTemplate;

    public EventPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publishOrderCreated(OrderEventMessage message) {
        amqpTemplate.convertAndSend(
                RabbitMQConfig.ORDERHUB_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                message
        );
    }

    public void publishPaymentSuccess(PaymentEventMessage message) {
        amqpTemplate.convertAndSend(
                RabbitMQConfig.ORDERHUB_EXCHANGE,
                RabbitMQConfig.PAYMENT_SUCCESS_ROUTING_KEY,
                message
        );
    }

    public void publishPaymentFailed(PaymentEventMessage message) {
        amqpTemplate.convertAndSend(
                RabbitMQConfig.ORDERHUB_EXCHANGE,
                RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY,
                message
        );
    }
}
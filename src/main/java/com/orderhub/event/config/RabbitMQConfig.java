package com.orderhub.event.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDERHUB_EXCHANGE = "orderhub.exchange";

    public static final String NOTIFICATION_QUEUE = "notification.queue";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    @Bean
    public TopicExchange orderHubExchange() {
        return new TopicExchange(ORDERHUB_EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .build();
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(orderHubExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(orderHubExchange())
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(orderHubExchange())
                .with(PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
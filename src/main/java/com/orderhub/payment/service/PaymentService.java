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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final EventPublisher eventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            PaymentMapper paymentMapper,
            EventPublisher eventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.paymentMapper = paymentMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentResponse fakePay(String email, FakePaymentRequest request) {
        User user = getUserByEmail(email);

        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if ("PAID".equals(order.getStatus())) {
            throw new BusinessException("Order has already been paid");
        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new BusinessException("Cancelled order cannot be paid");
        }

        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new BusinessException("Payment already exists for this order");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(order.getTotalAmount());
        payment.setTransactionCode(generateTransactionCode());

        if (request.isSuccess()) {
            payment.setStatus("SUCCESS");
            payment.setPaidAt(LocalDateTime.now());

            order.setStatus("PAID");
            orderRepository.save(order);
        } else {
            payment.setStatus("FAILED");
        }

        Payment savedPayment = paymentRepository.save(payment);

        publishPaymentEvent(user, order, savedPayment);

        return paymentMapper.toResponse(savedPayment);
    }

    public PaymentResponse getPaymentByOrder(String email, Long orderId) {
        User user = getUserByEmail(email);

        Payment payment = paymentRepository.findByOrderIdAndOrderUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        return paymentMapper.toResponse(payment);
    }

    private void publishPaymentEvent(User user, Order order, Payment payment) {
        PaymentEventMessage eventMessage = new PaymentEventMessage(
                user.getId(),
                order.getId(),
                order.getOrderCode(),
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus()
        );

        if ("SUCCESS".equals(payment.getStatus())) {
            eventPublisher.publishPaymentSuccess(eventMessage);
            return;
        }

        if ("FAILED".equals(payment.getStatus())) {
            eventPublisher.publishPaymentFailed(eventMessage);
        }
    }

    private String generateTransactionCode() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
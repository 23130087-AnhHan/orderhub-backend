package com.orderhub.payment.service;

import com.orderhub.common.exception.BusinessException;
import com.orderhub.common.exception.ResourceNotFoundException;
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
import com.orderhub.notification.service.NotificationService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;


    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            PaymentMapper paymentMapper,
            NotificationService notificationService
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.paymentMapper = paymentMapper;
        this.notificationService = notificationService;
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

        if (request.isSuccess()) {
            payment.setStatus("SUCCESS");
            payment.setTransactionCode(generateTransactionCode());
            payment.setPaidAt(LocalDateTime.now());

            order.setStatus("PAID");
            orderRepository.save(order);

            notificationService.createNotification(
                    user,
                    "PAYMENT_SUCCESS",
                    "Payment successful",
                    "Your payment for order " + order.getOrderCode() + " was successful."
            );
        } else {
            payment.setStatus("FAILED");
            payment.setTransactionCode(generateTransactionCode());

            notificationService.createNotification(
                    user,
                    "PAYMENT_FAILED",
                    "Payment failed",
                    "Your payment for order " + order.getOrderCode() + " failed. Please try again."
            );
        }

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(savedPayment);
    }

    public PaymentResponse getPaymentByOrder(String email, Long orderId) {
        User user = getUserByEmail(email);

        Payment payment = paymentRepository.findByOrderIdAndOrderUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        return paymentMapper.toResponse(payment);
    }

    private String generateTransactionCode() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
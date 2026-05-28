package com.orderhub.payment.controller;

import com.orderhub.common.response.ApiResponse;
import com.orderhub.payment.dto.FakePaymentRequest;
import com.orderhub.payment.dto.PaymentResponse;
import com.orderhub.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/fake-pay")
    public ApiResponse<PaymentResponse> fakePay(
            Authentication authentication,
            @Valid @RequestBody FakePaymentRequest request
    ) {
        PaymentResponse response = paymentService.fakePay(authentication.getName(), request);
        return ApiResponse.success("Payment processed successfully", response);
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<PaymentResponse> getPaymentByOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        PaymentResponse response = paymentService.getPaymentByOrder(authentication.getName(), orderId);
        return ApiResponse.success("Payment retrieved successfully", response);
    }
}
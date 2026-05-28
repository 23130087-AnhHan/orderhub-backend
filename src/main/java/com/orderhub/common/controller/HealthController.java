package com.orderhub.common.controller;

import com.orderhub.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Health check successful", "OrderHub Backend is running");
    }
}
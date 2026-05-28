package com.orderhub.notification.controller;

import com.orderhub.common.response.ApiResponse;
import com.orderhub.common.response.PageResponse;
import com.orderhub.notification.dto.NotificationFilterRequest;
import com.orderhub.notification.dto.NotificationResponse;
import com.orderhub.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> getMyNotifications(
            Authentication authentication,
            NotificationFilterRequest request
    ) {
        PageResponse<NotificationResponse> response =
                notificationService.getMyNotifications(authentication.getName(), request);

        return ApiResponse.success("Notifications retrieved successfully", response);
    }

    @PutMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable String id
    ) {
        NotificationResponse response = notificationService.markAsRead(authentication.getName(), id);
        return ApiResponse.success("Notification marked as read", response);
    }
}
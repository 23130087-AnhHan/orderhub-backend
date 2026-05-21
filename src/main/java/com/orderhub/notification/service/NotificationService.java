package com.orderhub.notification.service;

import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.common.response.PageResponse;
import com.orderhub.notification.dto.NotificationFilterRequest;
import com.orderhub.notification.dto.NotificationResponse;
import com.orderhub.notification.entity.Notification;
import com.orderhub.notification.mapper.NotificationMapper;
import com.orderhub.notification.repository.NotificationRepository;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    public PageResponse<NotificationResponse> getMyNotifications(
            String email,
            NotificationFilterRequest request
    ) {
        User user = getUserByEmail(email);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<Notification> notificationPage =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        return PageResponse.of(
                notificationPage.getContent()
                        .stream()
                        .map(notificationMapper::toResponse)
                        .toList(),
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages()
        );
    }

    @Transactional
    public NotificationResponse markAsRead(String email, Long notificationId) {
        User user = getUserByEmail(email);

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);

        Notification savedNotification = notificationRepository.save(notification);

        return notificationMapper.toResponse(savedNotification);
    }

    @Transactional
    public void createNotification(User user, String type, String title, String message) {
        Notification notification = new Notification(user, type, title, message);
        notificationRepository.save(notification);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
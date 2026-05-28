package com.orderhub.notification.service;

import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.common.response.PageResponse;
import com.orderhub.notification.document.Notification;
import com.orderhub.notification.dto.NotificationFilterRequest;
import com.orderhub.notification.dto.NotificationResponse;
import com.orderhub.notification.mapper.NotificationMapper;
import com.orderhub.notification.repository.NotificationRepository;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;
    private NotificationResponse notificationResponse;
    private NotificationFilterRequest filterRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFullName("John Doe");

        notification = new Notification();
        notification.setId("notification-id-1");
        notification.setUserId(1L);
        notification.setType("ORDER_CREATED");
        notification.setTitle("Order created");
        notification.setMessage("Your order ORD-20260528-ABC12345 has been created and is waiting for payment.");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationResponse = new NotificationResponse();
        notificationResponse.setId("notification-id-1");
        notificationResponse.setType("ORDER_CREATED");
        notificationResponse.setTitle("Order created");
        notificationResponse.setMessage("Your order ORD-20260528-ABC12345 has been created and is waiting for payment.");
        notificationResponse.setRead(false);
        notificationResponse.setCreatedAt(notification.getCreatedAt());

        filterRequest = new NotificationFilterRequest();
        filterRequest.setPage(0);
        filterRequest.setSize(10);
    }

    @Test
    void getMyNotifications_ShouldReturnPageResponse_WhenUserExists() {
        // Arrange
        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(user.getId()), any(Pageable.class)))
                .thenReturn(notificationPage);

        when(notificationMapper.toResponse(notification))
                .thenReturn(notificationResponse);

        // Act
        PageResponse<NotificationResponse> response =
                notificationService.getMyNotifications("john@example.com", filterRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals("notification-id-1", response.getItems().get(0).getId());
        assertEquals("ORDER_CREATED", response.getItems().get(0).getType());
        assertEquals("Order created", response.getItems().get(0).getTitle());
        assertFalse(response.getItems().get(0).isRead());

        verify(userRepository).findByEmail("john@example.com");
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(eq(user.getId()), any(Pageable.class));
        verify(notificationMapper).toResponse(notification);
    }

    @Test
    void getMyNotifications_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.getMyNotifications("unknown@example.com", filterRequest)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("unknown@example.com");
        verify(notificationRepository, never()).findByUserIdOrderByCreatedAtDesc(anyLong(), any(Pageable.class));
        verify(notificationMapper, never()).toResponse(any(Notification.class));
    }

    @Test
    void markAsRead_ShouldReturnNotificationResponse_WhenNotificationBelongsToUser() {
        // Arrange
        NotificationResponse readNotificationResponse = new NotificationResponse();
        readNotificationResponse.setId("notification-id-1");
        readNotificationResponse.setType("ORDER_CREATED");
        readNotificationResponse.setTitle("Order created");
        readNotificationResponse.setMessage("Your order ORD-20260528-ABC12345 has been created and is waiting for payment.");
        readNotificationResponse.setRead(true);
        readNotificationResponse.setCreatedAt(notification.getCreatedAt());

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(notificationRepository.findByIdAndUserId("notification-id-1", user.getId()))
                .thenReturn(Optional.of(notification));

        when(notificationRepository.save(notification))
                .thenReturn(notification);

        when(notificationMapper.toResponse(notification))
                .thenReturn(readNotificationResponse);

        // Act
        NotificationResponse response =
                notificationService.markAsRead("john@example.com", "notification-id-1");

        // Assert
        assertNotNull(response);
        assertEquals("notification-id-1", response.getId());
        assertTrue(response.isRead());
        assertTrue(notification.isRead());

        verify(userRepository).findByEmail("john@example.com");
        verify(notificationRepository).findByIdAndUserId("notification-id-1", user.getId());
        verify(notificationRepository).save(notification);
        verify(notificationMapper).toResponse(notification);
    }

    @Test
    void markAsRead_ShouldThrowResourceNotFoundException_WhenNotificationDoesNotBelongToUser() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(notificationRepository.findByIdAndUserId("wrong-notification-id", user.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsRead("john@example.com", "wrong-notification-id")
        );

        assertEquals("Notification not found", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(notificationRepository).findByIdAndUserId("wrong-notification-id", user.getId());
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationMapper, never()).toResponse(any(Notification.class));
    }

    @Test
    void markAsRead_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsRead("unknown@example.com", "notification-id-1")
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("unknown@example.com");
        verify(notificationRepository, never()).findByIdAndUserId(anyString(), anyLong());
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationMapper, never()).toResponse(any(Notification.class));
    }

    @Test
    void createNotification_ShouldSaveNotification_WhenUserIsValid() {
        // Act
        notificationService.createNotification(
                user,
                "PAYMENT_SUCCESS",
                "Payment successful",
                "Your payment for order ORD-20260528-ABC12345 was successful."
        );

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();

        assertEquals(1L, savedNotification.getUserId());
        assertEquals("PAYMENT_SUCCESS", savedNotification.getType());
        assertEquals("Payment successful", savedNotification.getTitle());
        assertEquals("Your payment for order ORD-20260528-ABC12345 was successful.", savedNotification.getMessage());
        assertFalse(savedNotification.isRead());
        assertNotNull(savedNotification.getCreatedAt());
    }
}
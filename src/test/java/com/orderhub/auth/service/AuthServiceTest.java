package com.orderhub.auth.service;

import com.orderhub.auth.dto.AuthResponse;
import com.orderhub.auth.dto.LoginRequest;
import com.orderhub.auth.dto.RegisterRequest;
import com.orderhub.common.exception.BusinessException;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.security.JwtService;
import com.orderhub.user.entity.Role;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.RoleRepository;
import com.orderhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Role userRole;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setPhone("0909123456");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("Password123");

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encoded-password");
        user.setPhone("0909123456");
        user.setRoles(Set.of(userRole));
    }

    @Test
    void register_ShouldReturnAuthResponse_WhenRequestIsValid() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("fake-jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getAccessToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("John Doe", savedUser.getFullName());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("0909123456", savedUser.getPhone());
        assertTrue(savedUser.getRoles().contains(userRole));

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void register_ShouldThrowBusinessException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository, never()).findByName(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(userDetails.getUsername())
                .thenReturn(loginRequest.getEmail());

        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(userDetails))
                .thenReturn("fake-jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getAccessToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication).getPrincipal();
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtService).generateToken(userDetails);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_ShouldThrowResourceNotFoundException_WhenEmailDoesNotExist() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(userDetails.getUsername())
                .thenReturn(loginRequest.getEmail());

        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("User not found", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication).getPrincipal();
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtService, never()).generateToken(any(UserDetails.class));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_ShouldThrowBusinessException_WhenPasswordIsWrong() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BusinessException("Invalid email or password"));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication, never()).getPrincipal();
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any(UserDetails.class));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
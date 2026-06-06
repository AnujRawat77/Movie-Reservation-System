package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.request.LoginRequest;
import com.movie_reservation.MovieReservationSystem.dto.request.RegisterRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.AuthResponse;
import com.movie_reservation.MovieReservationSystem.entity.User;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.UserRepository;
import com.movie_reservation.MovieReservationSystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .passwordHash("hashed")
                .role("USER")
                .build();
    }

    @Test
    void register_withNewEmail_returnsAuthResponse() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken("alice@example.com", "USER")).thenReturn("token123");

        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("secret");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getName()).isEqualTo("Alice");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withDuplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest();
        req.setEmail("alice@example.com");
        req.setPassword("secret");
        req.setName("Alice");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CONFLICT");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withCorrectCredentials_returnsAuthResponse() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("alice@example.com", "USER")).thenReturn("tokenXYZ");

        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("secret");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("tokenXYZ");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void login_withUnknownEmail_throwsResourceNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setEmail("unknown@example.com");
        req.setPassword("secret");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_CREDENTIALS");
    }
}

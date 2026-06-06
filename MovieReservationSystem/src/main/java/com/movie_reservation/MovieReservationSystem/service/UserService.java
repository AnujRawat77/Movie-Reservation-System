package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.response.ReservationResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.UserResponse;
import com.movie_reservation.MovieReservationSystem.entity.User;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.ReservationRepository;
import com.movie_reservation.MovieReservationSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public UserResponse toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        user = userRepository.save(user);
        return toResponse(user);
    }

    public List<ReservationResponse> getUserBookings(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return reservationRepository.findByUserId(userId).stream()
                .map(r -> reservationService.mapToResponse(r))
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateRole(Long id, String role) {
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new BusinessException("INVALID_ROLE", "Role must be USER or ADMIN");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setRole(role);
        user = userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

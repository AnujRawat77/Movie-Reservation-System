package com.movie_reservation.MovieReservationSystem.controller;

import com.movie_reservation.MovieReservationSystem.constant.UserRole;
import com.movie_reservation.MovieReservationSystem.dto.request.ReservationRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ApiResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.ReservationResponse;
import com.movie_reservation.MovieReservationSystem.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReservationResponse response = reservationService.createReservation(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Reservation created successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReservationResponse> reservations = reservationService.getUserReservations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN));
        ReservationResponse response = reservationService.getReservationById(id, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN));
        ReservationResponse response = reservationService.cancelReservation(id, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response, "Reservation cancelled successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAllReservations() {
        List<ReservationResponse> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }
}

package com.movie_reservation.MovieReservationSystem.controller;

import com.movie_reservation.MovieReservationSystem.dto.request.UpdateProfileRequest;
import com.movie_reservation.MovieReservationSystem.dto.request.UserRoleRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ApiResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.ReservationResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.UserResponse;
import com.movie_reservation.MovieReservationSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleRequest request) {
        UserResponse response = userService.updateRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success(response, "Role updated successfully"));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(@PathVariable Long id) {
        UserResponse response = userService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User status updated"));
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getUserBookings(@PathVariable Long id) {
        List<ReservationResponse> bookings = userService.getUserBookings(id);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }
}

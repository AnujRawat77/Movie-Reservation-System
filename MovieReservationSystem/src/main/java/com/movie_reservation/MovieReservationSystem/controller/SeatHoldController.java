package com.movie_reservation.MovieReservationSystem.controller;

import com.movie_reservation.MovieReservationSystem.dto.request.CreateHoldRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ApiResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.ReservationResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.SeatHoldResponse;
import com.movie_reservation.MovieReservationSystem.service.SeatHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/holds")
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    @PostMapping
    public ResponseEntity<ApiResponse<SeatHoldResponse>> createHold(
            @Valid @RequestBody CreateHoldRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SeatHoldResponse response = seatHoldService.createHold(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Seats held successfully"));
    }

    @GetMapping("/{holdId}")
    public ResponseEntity<ApiResponse<SeatHoldResponse>> getHold(
            @PathVariable UUID holdId,
            @AuthenticationPrincipal UserDetails userDetails) {
        SeatHoldResponse response = seatHoldService.getHold(holdId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{holdId}/refresh")
    public ResponseEntity<ApiResponse<SeatHoldResponse>> refreshHold(
            @PathVariable UUID holdId,
            @AuthenticationPrincipal UserDetails userDetails) {
        SeatHoldResponse response = seatHoldService.refreshHold(holdId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Hold refreshed"));
    }

    @PostMapping("/{holdId}/confirm")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirmHold(
            @PathVariable UUID holdId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReservationResponse response = seatHoldService.confirmHold(holdId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Booking confirmed successfully"));
    }

    @DeleteMapping("/{holdId}")
    public ResponseEntity<ApiResponse<Void>> releaseHold(
            @PathVariable UUID holdId,
            @AuthenticationPrincipal UserDetails userDetails) {
        seatHoldService.releaseHold(holdId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Hold released"));
    }
}

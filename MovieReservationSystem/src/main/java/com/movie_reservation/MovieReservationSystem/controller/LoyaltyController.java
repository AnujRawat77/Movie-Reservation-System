package com.movie_reservation.MovieReservationSystem.controller;

import com.movie_reservation.MovieReservationSystem.dto.request.RedeemPointsRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ApiResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.LoyaltyBalanceResponse;
import com.movie_reservation.MovieReservationSystem.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping
    public ResponseEntity<ApiResponse<LoyaltyBalanceResponse>> getBalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        LoyaltyBalanceResponse balance = loyaltyService.getBalance(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<LoyaltyBalanceResponse>> redeem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RedeemPointsRequest request) {
        LoyaltyBalanceResponse balance = loyaltyService.redeemPoints(userDetails.getUsername(), request.getPoints());
        return ResponseEntity.ok(ApiResponse.success(balance, "Points redeemed successfully"));
    }
}

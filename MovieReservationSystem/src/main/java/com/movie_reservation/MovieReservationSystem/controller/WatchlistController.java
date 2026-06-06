package com.movie_reservation.MovieReservationSystem.controller;

import com.movie_reservation.MovieReservationSystem.dto.response.ApiResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.WatchlistItemResponse;
import com.movie_reservation.MovieReservationSystem.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping("/api/users/me/watchlist")
    public ResponseEntity<ApiResponse<List<WatchlistItemResponse>>> getWatchlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<WatchlistItemResponse> items = watchlistService.getWatchlist(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping("/api/movies/{movieId}/watchlist")
    public ResponseEntity<ApiResponse<Void>> addToWatchlist(
            @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetails userDetails) {
        watchlistService.addToWatchlist(userDetails.getUsername(), movieId);
        return ResponseEntity.ok(ApiResponse.success(null, "Added to watchlist"));
    }

    @DeleteMapping("/api/movies/{movieId}/watchlist")
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetails userDetails) {
        watchlistService.removeFromWatchlist(userDetails.getUsername(), movieId);
        return ResponseEntity.ok(ApiResponse.success(null, "Removed from watchlist"));
    }

    @GetMapping("/api/movies/{movieId}/watchlist/status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getWatchlistStatus(
            @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean inWatchlist = watchlistService.isInWatchlist(userDetails.getUsername(), movieId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("inWatchlist", inWatchlist)));
    }
}

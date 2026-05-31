package com.movie_reservation.MovieReservationSystem.controller;

import com.movie_reservation.MovieReservationSystem.dto.response.ApiResponse;
import com.movie_reservation.MovieReservationSystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Map<String, Object> report = reportService.getRevenue(from, to);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/capacity/{showtimeId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCapacity(@PathVariable Long showtimeId) {
        Map<String, Object> report = reportService.getCapacity(showtimeId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/top-movies")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopMovies() {
        List<Map<String, Object>> topMovies = reportService.getTopMovies();
        return ResponseEntity.ok(ApiResponse.success(topMovies));
    }
}

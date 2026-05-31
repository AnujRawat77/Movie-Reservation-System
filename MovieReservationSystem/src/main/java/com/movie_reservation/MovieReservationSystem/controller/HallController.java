package com.movie_reservation.MovieReservationSystem.controller;

import com.movie_reservation.MovieReservationSystem.dto.request.HallRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ApiResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.HallResponse;
import com.movie_reservation.MovieReservationSystem.service.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HallResponse>>> getAllHalls() {
        return ResponseEntity.ok(ApiResponse.success(hallService.getAllHalls()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HallResponse>> getHall(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(hallService.getHallById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HallResponse>> createHall(@Valid @RequestBody HallRequest body) {
        HallResponse hall = hallService.createHall(body.getName(), body.getTotalRows(), body.getSeatsPerRow());
        return ResponseEntity.ok(ApiResponse.success(hall, "Hall created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HallResponse>> updateHall(
            @PathVariable Long id,
            @Valid @RequestBody HallRequest body) {
        HallResponse hall = hallService.updateHall(id, body);
        return ResponseEntity.ok(ApiResponse.success(hall, "Hall updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Hall deleted successfully"));
    }
}

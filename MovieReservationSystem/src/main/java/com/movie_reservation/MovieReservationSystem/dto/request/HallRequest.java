package com.movie_reservation.MovieReservationSystem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HallRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "totalRows is required")
    @Min(value = 1, message = "totalRows must be at least 1")
    private Integer totalRows;

    @NotNull(message = "seatsPerRow is required")
    @Min(value = 1, message = "seatsPerRow must be at least 1")
    private Integer seatsPerRow;
}

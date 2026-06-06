package com.movie_reservation.MovieReservationSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BulkShowtimeRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Hall ID is required")
    private Long hallId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotEmpty(message = "At least one show time is required")
    private List<LocalTime> times;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotEmpty(message = "At least one day of week is required")
    private List<String> daysOfWeek;
}

package com.movie_reservation.MovieReservationSystem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class MovieRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String tagline;

    private String description;

    private String posterUrl;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private int durationMinutes;

    private double rating;

    private int year;

    private String language;

    private String synopsis;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(now|soon|old)$", message = "Status must be one of: now, soon, old")
    private String status;

    private String releaseDate;

    @NotNull(message = "Genre IDs are required")
    private List<Long> genreIds;
}

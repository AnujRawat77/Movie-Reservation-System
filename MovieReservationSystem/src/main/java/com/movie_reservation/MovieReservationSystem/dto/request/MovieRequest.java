package com.movie_reservation.MovieReservationSystem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
public class MovieRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String tagline;

    private String description;

    @URL(message = "Poster URL must be a valid URL")
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

    @URL(message = "Trailer URL must be a valid URL")
    private String trailerUrl;

    private String director;

    private String cast;

    @Pattern(regexp = "^(G|PG|PG-13|R|NC-17)$", message = "Censor rating must be G, PG, PG-13, R, or NC-17")
    private String censorRating;

    @NotNull(message = "Genre IDs are required")
    private List<Long> genreIds;
}

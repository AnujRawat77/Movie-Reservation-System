package com.movie_reservation.MovieReservationSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

    private Long id;
    private String title;
    private String tagline;
    private String description;
    private String posterUrl;
    private int durationMinutes;
    private double rating;
    private int year;
    private String language;
    private String synopsis;
    private String status;
    private String releaseDate;
    private boolean deleted;
    private String trailerUrl;
    private String director;
    private String cast;
    private String censorRating;
    private List<GenreResponse> genres;
    private List<ShowtimeResponse> showtimes; // null for list view, populated for detail view
}

package com.movie_reservation.MovieReservationSystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String posterUrl;

    private int durationMinutes;

    private double rating;

    @Column(name = "release_year")
    private int year;

    private String language;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(nullable = false)
    private String status; // "now" or "soon"

    private String releaseDate; // nullable, e.g. "Dec 12"

    private String trailerUrl;

    private String director;

    @Column(columnDefinition = "TEXT")
    private String cast;

    private String censorRating; // G, PG, PG-13, R, NC-17

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

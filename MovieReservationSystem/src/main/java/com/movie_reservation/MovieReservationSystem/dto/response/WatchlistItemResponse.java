package com.movie_reservation.MovieReservationSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItemResponse {
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private LocalDateTime addedAt;
}

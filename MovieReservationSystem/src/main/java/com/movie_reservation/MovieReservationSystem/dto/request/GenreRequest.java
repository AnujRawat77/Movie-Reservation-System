package com.movie_reservation.MovieReservationSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenreRequest {

    @NotBlank(message = "Genre name is required")
    private String name;
}

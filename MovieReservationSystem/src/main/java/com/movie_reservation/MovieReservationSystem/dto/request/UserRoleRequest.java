package com.movie_reservation.MovieReservationSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRoleRequest {

    @NotBlank(message = "Role is required")
    private String role;
}

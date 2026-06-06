package com.movie_reservation.MovieReservationSystem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RedeemPointsRequest {

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Must redeem at least 1 point")
    private Integer points;
}

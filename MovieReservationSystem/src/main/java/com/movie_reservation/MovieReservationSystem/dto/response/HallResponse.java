package com.movie_reservation.MovieReservationSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HallResponse {

    private Long id;
    private String name;
    private int totalRows;
    private int seatsPerRow;
    private int capacity;
}

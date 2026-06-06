package com.movie_reservation.MovieReservationSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransactionResponse {
    private Long id;
    private String type;
    private Integer points;
    private String description;
    private UUID reservationId;
    private LocalDateTime createdAt;
}

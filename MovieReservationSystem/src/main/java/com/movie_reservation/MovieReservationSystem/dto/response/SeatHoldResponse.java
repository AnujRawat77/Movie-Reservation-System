package com.movie_reservation.MovieReservationSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldResponse {

    private String holdId;
    private Long showtimeId;
    private String movieTitle;
    private String hallName;
    private String showDate;
    private String showTime;
    private List<SeatInfo> seats;
    private String status;       // ACTIVE | CONFIRMED | EXPIRED | RELEASED
    private LocalDateTime expiresAt;
    private long expiresInSeconds;
    private BigDecimal totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private Long seatId;
        private String rowLabel;
        private int seatNumber;
        private String seatType;
    }
}

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
public class ReservationResponse {

    private String id;
    private Long userId;
    private String userName;
    private Long showtimeId;
    private String movieTitle;
    private String hallName;
    private String showDate;
    private String showTime;
    private List<SeatInfo> seats;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public record SeatInfo(String rowLabel, int seatNumber, String seatType) {}
}

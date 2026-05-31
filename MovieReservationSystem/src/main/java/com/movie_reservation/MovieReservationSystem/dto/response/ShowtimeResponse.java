package com.movie_reservation.MovieReservationSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeResponse {

    private Long id;
    private Long movieId;
    private Long hallId;
    private String hallName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private String status;
    private String date;  // "Today", "Tomorrow", or formatted date
    private String time;  // "HH:mm"

    public static String computeDate(LocalDateTime startTime) {
        LocalDate today = LocalDate.now();
        LocalDate showDate = startTime.toLocalDate();
        if (showDate.equals(today)) {
            return "Today";
        } else if (showDate.equals(today.plusDays(1))) {
            return "Tomorrow";
        } else {
            return showDate.format(DateTimeFormatter.ofPattern("MMM dd"));
        }
    }

    public static String computeTime(LocalDateTime startTime) {
        return startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}

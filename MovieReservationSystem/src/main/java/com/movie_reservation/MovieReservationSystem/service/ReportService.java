package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.entity.Showtime;
import com.movie_reservation.MovieReservationSystem.repository.ReservationRepository;
import com.movie_reservation.MovieReservationSystem.repository.ReservationSeatRepository;
import com.movie_reservation.MovieReservationSystem.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ShowtimeRepository showtimeRepository;

    public Map<String, Object> getRevenue(LocalDate from, LocalDate to) {
        LocalDateTime fromTime = (from != null) ? from.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime toTime = (to != null) ? to.plusDays(1).atStartOfDay() : LocalDateTime.now();

        // Single DB query filtered by date range — no more full table scan + Java filtering
        Map<String, BigDecimal> revenueByMovie = reservationRepository
                .findConfirmedInRange(fromTime, toTime)
                .stream()
                .collect(Collectors.groupingBy(
                        r -> r.getShowtime().getMovie().getTitle(),
                        Collectors.reducing(BigDecimal.ZERO,
                                r -> r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO,
                                BigDecimal::add)
                ));

        BigDecimal totalRevenue = revenueByMovie.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> byMovie = revenueByMovie.entrySet().stream()
                .map(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("movieTitle", e.getKey());
                    entry.put("revenue", e.getValue());
                    return entry;
                })
                .collect(Collectors.toList());

        log.debug("getRevenue: from={} to={} totalRevenue={}", fromTime.toLocalDate(), toTime.toLocalDate(), totalRevenue);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("from", fromTime.toLocalDate());
        report.put("to", toTime.toLocalDate());
        report.put("totalRevenue", totalRevenue);
        report.put("byMovie", byMovie);

        return report;
    }

    public Map<String, Object> getCapacity(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", showtimeId));

        int totalSeats = showtime.getHall().getTotalRows() * showtime.getHall().getSeatsPerRow();
        long bookedSeats = reservationSeatRepository.countConfirmedByShowtimeId(showtimeId);
        double occupancy = totalSeats > 0 ? ((double) bookedSeats / totalSeats) * 100.0 : 0.0;

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("showtimeId", showtimeId);
        report.put("movieTitle", showtime.getMovie().getTitle());
        report.put("hallName", showtime.getHall().getName());
        report.put("startTime", showtime.getStartTime());
        report.put("totalSeats", totalSeats);
        report.put("bookedSeats", bookedSeats);
        report.put("availableSeats", totalSeats - bookedSeats);
        report.put("occupancyPercentage", Math.round(occupancy * 100.0) / 100.0);

        return report;
    }

    public List<Map<String, Object>> getTopMovies() {
        // Single GROUP BY query — replaces N+1 per-movie sumRevenueByMovieTitle calls
        return reservationRepository
                .findTopMoviesByRevenue(PageRequest.of(0, 10))
                .stream()
                .map(row -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("movieId", row[0]);
                    entry.put("title", row[1]);
                    entry.put("revenue", row[2]);
                    return entry;
                })
                .collect(Collectors.toList());
    }
}

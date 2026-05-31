package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.entity.Movie;
import com.movie_reservation.MovieReservationSystem.entity.Showtime;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.MovieRepository;
import com.movie_reservation.MovieReservationSystem.repository.ReservationRepository;
import com.movie_reservation.MovieReservationSystem.repository.ReservationSeatRepository;
import com.movie_reservation.MovieReservationSystem.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    public Map<String, Object> getRevenue(LocalDate from, LocalDate to) {
        LocalDateTime fromTime = (from != null) ? from.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime toTime = (to != null) ? to.plusDays(1).atStartOfDay() : LocalDateTime.now();

        // Group reservations by movie title and sum revenue
        Map<String, BigDecimal> revenueByMovie = reservationRepository.findAll().stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .filter(r -> !r.getCreatedAt().isBefore(fromTime) && r.getCreatedAt().isBefore(toTime))
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
        List<Movie> movies = movieRepository.findAll();

        return movies.stream()
                .map(movie -> {
                    BigDecimal revenue = Optional.ofNullable(
                            reservationRepository.sumRevenueByMovieTitle(movie.getTitle())
                    ).orElse(BigDecimal.ZERO);

                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("movieId", movie.getId());
                    entry.put("title", movie.getTitle());
                    entry.put("revenue", revenue);
                    return entry;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")))
                .limit(10)
                .collect(Collectors.toList());
    }
}

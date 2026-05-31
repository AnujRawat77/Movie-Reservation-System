package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.request.ShowtimeRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.SeatResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.ShowtimeResponse;
import com.movie_reservation.MovieReservationSystem.entity.*;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ReservationRepository reservationRepository;

    public List<ShowtimeResponse> getShowtimesForMovie(Long movieId, String date) {
        List<Showtime> showtimes = showtimeRepository.findByMovieIdAndStatus(movieId, "SCHEDULED");

        if (StringUtils.hasText(date)) {
            showtimes = showtimes.stream()
                    .filter(s -> {
                        String showDate = s.getStartTime().toLocalDate()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE);
                        return showDate.equals(date);
                    })
                    .collect(Collectors.toList());
        }

        return showtimes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ShowtimeResponse> listAll(Long movieId, Long hallId, String status,
                                          LocalDateTime from, LocalDateTime to) {
        return showtimeRepository.findAllByOrderByStartTimeAsc().stream()
                .filter(s -> movieId == null || s.getMovie().getId().equals(movieId))
                .filter(s -> hallId == null || s.getHall().getId().equals(hallId))
                .filter(s -> status == null || status.isBlank() || status.equals(s.getStatus()))
                .filter(s -> from == null || !s.getStartTime().isBefore(from))
                .filter(s -> to == null || !s.getStartTime().isAfter(to))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ShowtimeResponse getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", id));
        return toResponse(showtime);
    }

    public List<SeatResponse> getAvailableSeats(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", showtimeId));

        List<Seat> allSeats = seatRepository.findByHallId(showtime.getHall().getId());

        // Get set of booked seat IDs for this showtime
        Set<Long> bookedSeatIds = reservationSeatRepository.findByShowtimeId(showtimeId)
                .stream()
                .filter(rs -> "CONFIRMED".equals(rs.getReservation().getStatus()))
                .map(rs -> rs.getSeat().getId())
                .collect(Collectors.toSet());

        return allSeats.stream()
                .map(seat -> SeatResponse.builder()
                        .id(seat.getId())
                        .rowLabel(seat.getRowLabel())
                        .seatNumber(seat.getSeatNumber())
                        .seatType(seat.getSeatType())
                        .status(bookedSeatIds.contains(seat.getId()) ? "BOOKED" : "AVAILABLE")
                        .build())
                .collect(Collectors.toList());
    }

    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findByIdAndIsDeletedFalse(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", request.getMovieId()));

        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall", request.getHallId()));

        LocalDateTime endTime = request.getStartTime()
                .plusMinutes(movie.getDurationMinutes())
                .plusMinutes(15);

        boolean hasOverlap = showtimeRepository
                .existsByHallIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        hall.getId(), "SCHEDULED", endTime, request.getStartTime());

        if (hasOverlap) {
            throw new BusinessException("SCHEDULE_CONFLICT",
                    "The hall is already booked during this time slot");
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .hall(hall)
                .startTime(request.getStartTime())
                .endTime(endTime)
                .price(request.getPrice())
                .status("SCHEDULED")
                .build();

        showtime = showtimeRepository.save(showtime);
        return toResponse(showtime);
    }

    public ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", id));

        Movie movie = movieRepository.findByIdAndIsDeletedFalse(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", request.getMovieId()));

        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall", request.getHallId()));

        LocalDateTime endTime = request.getStartTime()
                .plusMinutes(movie.getDurationMinutes())
                .plusMinutes(15);

        // Check for overlap, excluding this showtime
        boolean hasOverlap = showtimeRepository
                .existsByHallIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        hall.getId(), "SCHEDULED", endTime, request.getStartTime());

        if (hasOverlap && !showtime.getHall().getId().equals(hall.getId())) {
            throw new BusinessException("SCHEDULE_CONFLICT",
                    "The hall is already booked during this time slot");
        }

        showtime.setMovie(movie);
        showtime.setHall(hall);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(endTime);
        showtime.setPrice(request.getPrice());

        showtime = showtimeRepository.save(showtime);
        return toResponse(showtime);
    }

    @Transactional
    public void cancelShowtime(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", id));

        if ("CANCELLED".equals(showtime.getStatus())) {
            throw new BusinessException("ALREADY_CANCELLED", "Showtime is already cancelled");
        }

        showtime.setStatus("CANCELLED");
        showtimeRepository.save(showtime);

        // Cancel all confirmed reservations for this showtime
        reservationRepository.findConfirmedByShowtimeId(id)
                .forEach(reservation -> {
                    reservation.setStatus("CANCELLED");
                    reservationRepository.save(reservation);
                });
    }

    public ShowtimeResponse toResponse(Showtime showtime) {
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovie().getId())
                .hallId(showtime.getHall().getId())
                .hallName(showtime.getHall().getName())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .price(showtime.getPrice())
                .status(showtime.getStatus())
                .date(ShowtimeResponse.computeDate(showtime.getStartTime()))
                .time(ShowtimeResponse.computeTime(showtime.getStartTime()))
                .build();
    }
}

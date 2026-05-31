package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.request.ReservationRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ReservationResponse;
import com.movie_reservation.MovieReservationSystem.entity.*;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String userEmail) {
        // 1. Validate showtime exists and is SCHEDULED
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", request.getShowtimeId()));

        if (!"SCHEDULED".equals(showtime.getStatus())) {
            throw new BusinessException("SHOWTIME_NOT_AVAILABLE", "Showtime is not available for booking");
        }

        // 2. Validate showtime is in the future
        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("SHOWTIME_PAST", "Cannot book a past showtime");
        }

        // 3. For each seat: check it's not already booked
        List<Seat> seats = new ArrayList<>();
        for (Long seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat", seatId));

            boolean alreadyBooked = reservationSeatRepository
                    .existsBySeatIdAndShowtimeIdAndReservationStatus(
                            seatId, request.getShowtimeId(), "CONFIRMED");

            if (alreadyBooked) {
                throw new BusinessException("SEAT_ALREADY_BOOKED",
                        "Seat " + seat.getRowLabel() + seat.getSeatNumber() + " is already booked");
            }

            seats.add(seat);
        }

        // 4. Get the user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // 5. Calculate total amount (PREMIUM seats get 1.5x multiplier)
        BigDecimal totalAmount = seats.stream()
                .map(seat -> {
                    BigDecimal seatPrice = showtime.getPrice();
                    if ("PREMIUM".equals(seat.getSeatType())) {
                        seatPrice = seatPrice.multiply(new BigDecimal("1.5"));
                    }
                    return seatPrice;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Create Reservation
        Reservation reservation = Reservation.builder()
                .user(user)
                .showtime(showtime)
                .status("CONFIRMED")
                .totalAmount(totalAmount)
                .build();
        reservation = reservationRepository.save(reservation);

        // 7. Create ReservationSeat for each seat
        final Reservation savedReservation = reservation;
        List<ReservationSeat> reservationSeats = seats.stream()
                .map(seat -> ReservationSeat.builder()
                        .reservation(savedReservation)
                        .seat(seat)
                        .showtime(showtime)
                        .build())
                .collect(Collectors.toList());

        reservationSeatRepository.saveAll(reservationSeats);

        return toResponse(reservation, reservationSeats);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(UUID id, String email, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));

        if (!isAdmin && !reservation.getUser().getEmail().equals(email)) {
            throw new BusinessException("UNAUTHORIZED", "You can only view your own reservations");
        }

        return toResponse(reservation, reservation.getReservationSeats());
    }

    public List<ReservationResponse> getUserReservations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return reservationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(r -> {
                    List<ReservationSeat> rSeats = reservationSeatRepository.findByShowtimeId(r.getShowtime().getId())
                            .stream()
                            .filter(rs -> rs.getReservation().getId().equals(r.getId()))
                            .collect(Collectors.toList());
                    return toResponse(r, rSeats);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservation(UUID id, String email, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));

        if (!isAdmin && !reservation.getUser().getEmail().equals(email)) {
            throw new BusinessException("UNAUTHORIZED", "You can only cancel your own reservations");
        }

        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BusinessException("ALREADY_CANCELLED", "Reservation is already cancelled");
        }

        if (!reservation.getShowtime().getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("SHOWTIME_PAST", "Cannot cancel a reservation for a past showtime");
        }

        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(r -> {
                    List<ReservationSeat> rSeats = r.getReservationSeats();
                    return toResponse(r, rSeats);
                })
                .collect(Collectors.toList());
    }

    private ReservationResponse toResponse(Reservation reservation, List<ReservationSeat> reservationSeats) {
        Showtime showtime = reservation.getShowtime();

        List<ReservationResponse.SeatInfo> seatInfos = reservationSeats.stream()
                .map(rs -> new ReservationResponse.SeatInfo(
                        rs.getSeat().getRowLabel(),
                        rs.getSeat().getSeatNumber(),
                        rs.getSeat().getSeatType()
                ))
                .collect(Collectors.toList());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return ReservationResponse.builder()
                .id(reservation.getId().toString())
                .userId(reservation.getUser().getId())
                .userName(reservation.getUser().getName())
                .showtimeId(showtime.getId())
                .movieTitle(showtime.getMovie().getTitle())
                .hallName(showtime.getHall().getName())
                .showDate(showtime.getStartTime().format(dateFormatter))
                .showTime(showtime.getStartTime().format(timeFormatter))
                .seats(seatInfos)
                .status(reservation.getStatus())
                .totalAmount(reservation.getTotalAmount())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}

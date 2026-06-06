package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.constant.ReservationStatus;
import com.movie_reservation.MovieReservationSystem.dto.request.ReservationRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ReservationResponse;
import com.movie_reservation.MovieReservationSystem.entity.*;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final SeatHoldService seatHoldService;

    @Value("${reservation.refund.full-hours:24}")
    private int fullRefundHours;

    @Value("${reservation.refund.partial-hours:2}")
    private int partialRefundHours;

    @Value("${reservation.refund.partial-percent:50}")
    private int partialRefundPercent;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String userEmail) {
        return seatHoldService.createDirectBooking(
                request.getShowtimeId(), request.getSeatIds(), userEmail);
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
    public ReservationResponse cancelReservation(UUID id, String email, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));

        if (!isAdmin && !reservation.getUser().getEmail().equals(email)) {
            throw new BusinessException("UNAUTHORIZED", "You can only cancel your own reservations");
        }

        if (ReservationStatus.CANCELLED.equals(reservation.getStatus())) {
            throw new BusinessException("ALREADY_CANCELLED", "Reservation is already cancelled");
        }

        LocalDateTime showStart = reservation.getShowtime().getStartTime();
        if (!showStart.isAfter(LocalDateTime.now())) {
            throw new BusinessException("SHOWTIME_PAST", "Cannot cancel a reservation for a past showtime");
        }

        long hoursUntilShow = Duration.between(LocalDateTime.now(), showStart).toHours();
        int refundPercent;
        if (hoursUntilShow >= fullRefundHours) {
            refundPercent = 100;
        } else if (hoursUntilShow >= partialRefundHours) {
            refundPercent = partialRefundPercent;
        } else {
            refundPercent = 0;
        }

        BigDecimal refundAmount = reservation.getTotalAmount()
                .multiply(BigDecimal.valueOf(refundPercent))
                .divide(BigDecimal.valueOf(100));

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setRefundPercentage(refundPercent);
        reservation.setRefundAmount(refundAmount);
        reservationRepository.save(reservation);

        log.info("Cancelled reservation id={} by user={}, refund={}% (${})", id, email, refundPercent, refundAmount);
        return toResponse(reservation, reservation.getReservationSeats());
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(r -> toResponse(r, r.getReservationSeats()))
                .collect(Collectors.toList());
    }

    public ReservationResponse mapToResponse(Reservation reservation) {
        return toResponse(reservation, reservation.getReservationSeats());
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
                .cancelledAt(reservation.getCancelledAt())
                .refundAmount(reservation.getRefundAmount())
                .refundPercentage(reservation.getRefundPercentage())
                .build();
    }
}

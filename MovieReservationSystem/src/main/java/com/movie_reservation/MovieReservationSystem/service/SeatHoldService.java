package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.constant.ReservationStatus;
import com.movie_reservation.MovieReservationSystem.constant.SeatType;
import com.movie_reservation.MovieReservationSystem.constant.ShowtimeStatus;
import com.movie_reservation.MovieReservationSystem.dto.request.CreateHoldRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ReservationResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.SeatHoldResponse;
import com.movie_reservation.MovieReservationSystem.entity.*;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.*;
import com.movie_reservation.MovieReservationSystem.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private static final int HOLD_DURATION_MINUTES = 5;

    @Value("${reservation.premium-seat-multiplier:1.5}")
    private BigDecimal premiumSeatMultiplier;

    private final SeatHoldRepository seatHoldRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final LoyaltyService loyaltyService;

    // ─── Create Hold ─────────────────────────────────────────────────────────

    @Transactional
    public SeatHoldResponse createHold(CreateHoldRequest request, String userEmail) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", request.getShowtimeId()));

        if (!ShowtimeStatus.SCHEDULED.equals(showtime.getStatus())) {
            throw new BusinessException("SHOWTIME_NOT_AVAILABLE", "Showtime is not available for booking");
        }
        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("SHOWTIME_PAST", "Cannot book a past showtime");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<Long> seatIds = request.getSeatIds();

        // Release any already-expired allocations for these seats so the slot is free
        seatAllocationRepository.deleteExpiredForSeats(request.getShowtimeId(), seatIds, LocalDateTime.now());

        // Check for already-confirmed bookings
        for (Long seatId : seatIds) {
            boolean alreadyBooked = reservationSeatRepository
                    .existsBySeatIdAndShowtimeIdAndReservationStatus(seatId, request.getShowtimeId(), ReservationStatus.CONFIRMED);
            if (alreadyBooked) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat", seatId));
                throw new BusinessException("SEAT_ALREADY_BOOKED",
                        "Seat " + seat.getRowLabel() + seat.getSeatNumber() + " is already booked");
            }
        }

        // Release any existing active hold from this same user for this showtime
        seatHoldRepository.findActiveHoldForUserAndShowtime(user.getId(), request.getShowtimeId(), LocalDateTime.now())
                .ifPresent(existing -> releaseHoldInternal(existing, user));

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES);

        SeatHold hold = SeatHold.builder()
                .user(user)
                .showtime(showtime)
                .status("ACTIVE")
                .expiresAt(expiresAt)
                .build();
        hold = seatHoldRepository.save(hold);

        List<Seat> seats = new ArrayList<>();
        for (Long seatId : seatIds) {
            seats.add(seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat", seatId)));
        }

        List<SeatAllocation> allocations = new ArrayList<>();
        for (Seat seat : seats) {
            allocations.add(SeatAllocation.builder()
                    .showtime(showtime)
                    .seat(seat)
                    .holdOwner(user)
                    .holdExpiresAt(expiresAt)
                    .seatHold(hold)
                    .build());
        }

        try {
            seatAllocationRepository.saveAll(allocations);
            seatAllocationRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            // Another concurrent request already holds one of these seats
            List<Long> conflicting = seatAllocationRepository
                    .findByShowtimeIdAndSeatIdIn(request.getShowtimeId(), seatIds)
                    .stream().map(sa -> sa.getSeat().getId()).collect(Collectors.toList());
            throw new BusinessException("SEAT_ALREADY_HELD",
                    "Seats already held by another user: " + conflicting);
        }

        return toResponse(hold, seats, showtime);
    }

    // ─── Release Hold ────────────────────────────────────────────────────────

    @Transactional
    public void releaseHold(UUID holdId, String userEmail) {
        SeatHold hold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold not found: " + holdId));

        if (!hold.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("HOLD_NOT_OWNED", "You do not own this hold");
        }
        if (ReservationStatus.CONFIRMED.equals(hold.getStatus())) {
            throw new BusinessException("HOLD_ALREADY_CONFIRMED", "Hold has already been confirmed as a booking");
        }

        User user = hold.getUser();
        releaseHoldInternal(hold, user);
    }

    private void releaseHoldInternal(SeatHold hold, User user) {
        seatAllocationRepository.deleteByHoldId(hold.getId());
        hold.setStatus("RELEASED");
        seatHoldRepository.save(hold);
    }

    // ─── Refresh Hold ────────────────────────────────────────────────────────

    @Transactional
    public SeatHoldResponse refreshHold(UUID holdId, String userEmail) {
        SeatHold hold = getOwnedActiveHold(holdId, userEmail);

        LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES);
        hold.setExpiresAt(newExpiry);
        seatHoldRepository.save(hold);

        // Update expiry on all allocations
        hold.getSeatAllocations().forEach(sa -> sa.setHoldExpiresAt(newExpiry));
        seatAllocationRepository.saveAll(hold.getSeatAllocations());

        List<Seat> seats = hold.getSeatAllocations().stream()
                .map(SeatAllocation::getSeat).collect(Collectors.toList());
        return toResponse(hold, seats, hold.getShowtime());
    }

    // ─── Confirm Hold (creates Reservation) ──────────────────────────────────

    @Transactional
    public ReservationResponse confirmHold(UUID holdId, String userEmail) {
        SeatHold hold = getOwnedActiveHold(holdId, userEmail);

        Showtime showtime = hold.getShowtime();
        User user = hold.getUser();

        List<SeatAllocation> allocations = seatAllocationRepository
                .findByShowtimeIdAndSeatIdIn(
                        showtime.getId(),
                        hold.getSeatAllocations().stream()
                                .map(sa -> sa.getSeat().getId())
                                .collect(Collectors.toList()));

        if (allocations.isEmpty()) {
            throw new BusinessException("HOLD_EXPIRED", "Hold has expired — seats are no longer reserved");
        }

        List<Seat> seats = allocations.stream().map(SeatAllocation::getSeat).collect(Collectors.toList());

        BigDecimal totalAmount = seats.stream()
                .map(seat -> {
                    BigDecimal price = showtime.getPrice();
                    return SeatType.PREMIUM.equals(seat.getSeatType()) ? price.multiply(premiumSeatMultiplier) : price;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Reservation reservation = Reservation.builder()
                .user(user)
                .showtime(showtime)
                .status("CONFIRMED")
                .totalAmount(totalAmount)
                .build();
        reservation = reservationRepository.save(reservation);

        final Reservation saved = reservation;
        List<ReservationSeat> reservationSeats = seats.stream()
                .map(seat -> ReservationSeat.builder()
                        .reservation(saved)
                        .seat(seat)
                        .showtime(showtime)
                        .build())
                .collect(Collectors.toList());
        reservationSeatRepository.saveAll(reservationSeats);

        // Free the hold allocations (seat is now permanently booked via ReservationSeat)
        seatAllocationRepository.deleteByHoldId(holdId);
        hold.setStatus("CONFIRMED");
        seatHoldRepository.save(hold);

        // Award loyalty points (10 pts per $1)
        loyaltyService.awardPoints(user.getId(), totalAmount, reservation.getId());

        return toReservationResponse(reservation, reservationSeats);
    }

    // ─── Internal confirm (used by old direct-book endpoint) ─────────────────

    @Transactional
    public ReservationResponse createDirectBooking(Long showtimeId, List<Long> seatIds, String userEmail) {
        CreateHoldRequest req = new CreateHoldRequest(showtimeId, seatIds);
        SeatHoldResponse holdResp = createHold(req, userEmail);
        return confirmHold(UUID.fromString(holdResp.getHoldId()), userEmail);
    }

    // ─── Get Hold ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SeatHoldResponse getHold(UUID holdId, String userEmail) {
        SeatHold hold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold not found: " + holdId));
        if (!hold.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("HOLD_NOT_OWNED", "You do not own this hold");
        }
        if (hold.isExpired() && "ACTIVE".equals(hold.getStatus())) {
            hold.setStatus("EXPIRED");
        }
        List<Seat> seats = hold.getSeatAllocations().stream()
                .map(SeatAllocation::getSeat).collect(Collectors.toList());
        return toResponse(hold, seats, hold.getShowtime());
    }

    // ─── Background cleanup ───────────────────────────────────────────────────

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanupExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = seatAllocationRepository.deleteExpired(now);
        if (deleted > 0) log.debug("Cleaned up {} expired seat allocations", deleted);

        seatHoldRepository.findExpiredActiveHolds(now).forEach(hold -> {
            hold.setStatus("EXPIRED");
            seatHoldRepository.save(hold);
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private SeatHold getOwnedActiveHold(UUID holdId, String userEmail) {
        SeatHold hold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold not found: " + holdId));
        if (!hold.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("HOLD_NOT_OWNED", "You do not own this hold");
        }
        if (!"ACTIVE".equals(hold.getStatus())) {
            throw new BusinessException("HOLD_EXPIRED",
                    "Hold is no longer active (status: " + hold.getStatus() + ")");
        }
        if (hold.isExpired()) {
            hold.setStatus("EXPIRED");
            seatHoldRepository.save(hold);
            seatAllocationRepository.deleteByHoldId(holdId);
            throw new BusinessException("HOLD_EXPIRED", "Hold has expired — please select seats again");
        }
        return hold;
    }

    private SeatHoldResponse toResponse(SeatHold hold, List<Seat> seats, Showtime showtime) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        BigDecimal totalAmount = seats.stream()
                .map(seat -> {
                    BigDecimal price = showtime.getPrice();
                    return SeatType.PREMIUM.equals(seat.getSeatType()) ? price.multiply(premiumSeatMultiplier) : price;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long secsLeft = Math.max(0, ChronoUnit.SECONDS.between(LocalDateTime.now(), hold.getExpiresAt()));
        if (!"ACTIVE".equals(hold.getStatus())) secsLeft = 0;

        return SeatHoldResponse.builder()
                .holdId(hold.getId().toString())
                .showtimeId(showtime.getId())
                .movieTitle(showtime.getMovie().getTitle())
                .hallName(showtime.getHall().getName())
                .showDate(showtime.getStartTime().format(dateFmt))
                .showTime(showtime.getStartTime().format(timeFmt))
                .seats(seats.stream()
                        .map(s -> SeatHoldResponse.SeatInfo.builder()
                                .seatId(s.getId())
                                .rowLabel(s.getRowLabel())
                                .seatNumber(s.getSeatNumber())
                                .seatType(s.getSeatType())
                                .build())
                        .collect(Collectors.toList()))
                .status(hold.getStatus())
                .expiresAt(hold.getExpiresAt())
                .expiresInSeconds(secsLeft)
                .totalAmount(totalAmount)
                .build();
    }

    private ReservationResponse toReservationResponse(Reservation reservation, List<ReservationSeat> rSeats) {
        Showtime showtime = reservation.getShowtime();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        List<ReservationResponse.SeatInfo> seatInfos = rSeats.stream()
                .map(rs -> new ReservationResponse.SeatInfo(
                        rs.getSeat().getRowLabel(),
                        rs.getSeat().getSeatNumber(),
                        rs.getSeat().getSeatType()))
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .id(reservation.getId().toString())
                .userId(reservation.getUser().getId())
                .userName(reservation.getUser().getName())
                .showtimeId(showtime.getId())
                .movieTitle(showtime.getMovie().getTitle())
                .hallName(showtime.getHall().getName())
                .showDate(showtime.getStartTime().format(dateFmt))
                .showTime(showtime.getStartTime().format(timeFmt))
                .seats(seatInfos)
                .status(reservation.getStatus())
                .totalAmount(reservation.getTotalAmount())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}

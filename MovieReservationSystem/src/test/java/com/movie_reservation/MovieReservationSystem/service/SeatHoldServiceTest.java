package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.request.CreateHoldRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ReservationResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.SeatHoldResponse;
import com.movie_reservation.MovieReservationSystem.entity.*;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatHoldServiceTest {

    @Mock private SeatHoldRepository seatHoldRepository;
    @Mock private SeatAllocationRepository seatAllocationRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationSeatRepository reservationSeatRepository;

    @InjectMocks
    private SeatHoldService seatHoldService;

    private User user;
    private Showtime showtime;
    private Seat seat1;
    private Seat seat2;
    private Hall hall;
    private Movie movie;

    @BeforeEach
    void setUp() {
        hall = Hall.builder().id(1L).name("Hall 1").totalRows(10).seatsPerRow(10).build();
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");

        showtime = Showtime.builder()
                .id(1L)
                .movie(movie)
                .hall(hall)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal("12.00"))
                .status("SCHEDULED")
                .build();

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .role("USER")
                .build();

        seat1 = Seat.builder().id(1L).rowLabel("A").seatNumber(1).seatType("REGULAR").hall(hall).build();
        seat2 = Seat.builder().id(2L).rowLabel("A").seatNumber(2).seatType("PREMIUM").hall(hall).build();
    }

    // ─── Create Hold ──────────────────────────────────────────────────────────

    @Test
    void createHold_withAvailableSeats_returnsActiveHold() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(reservationSeatRepository.existsBySeatIdAndShowtimeIdAndReservationStatus(anyLong(), anyLong(), eq("CONFIRMED")))
                .thenReturn(false);
        when(seatHoldRepository.findActiveHoldForUserAndShowtime(anyLong(), anyLong(), any())).thenReturn(Optional.empty());
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatRepository.findById(2L)).thenReturn(Optional.of(seat2));

        SeatHold savedHold = SeatHold.builder()
                .id(UUID.randomUUID())
                .user(user)
                .showtime(showtime)
                .status("ACTIVE")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .seatAllocations(new ArrayList<>())
                .build();
        when(seatHoldRepository.save(any())).thenReturn(savedHold);
        when(seatAllocationRepository.saveAll(any())).thenReturn(Collections.emptyList());

        CreateHoldRequest req = new CreateHoldRequest(1L, List.of(1L, 2L));
        SeatHoldResponse response = seatHoldService.createHold(req, "test@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getHoldId()).isEqualTo(savedHold.getId().toString());
        assertThat(response.getTotalAmount()).isEqualByComparingTo("30.00"); // 12 + 12*1.5
    }

    @Test
    void createHold_withAlreadyBookedSeat_throwsSeatAlreadyBooked() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(reservationSeatRepository.existsBySeatIdAndShowtimeIdAndReservationStatus(eq(1L), anyLong(), eq("CONFIRMED")))
                .thenReturn(true);
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));

        CreateHoldRequest req = new CreateHoldRequest(1L, List.of(1L));

        assertThatThrownBy(() -> seatHoldService.createHold(req, "test@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SEAT_ALREADY_BOOKED");
    }

    @Test
    void createHold_whenConcurrentInsertFails_throwsSeatAlreadyHeld() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(reservationSeatRepository.existsBySeatIdAndShowtimeIdAndReservationStatus(anyLong(), anyLong(), any()))
                .thenReturn(false);
        when(seatHoldRepository.findActiveHoldForUserAndShowtime(anyLong(), anyLong(), any())).thenReturn(Optional.empty());
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));

        SeatHold savedHold = SeatHold.builder()
                .id(UUID.randomUUID())
                .user(user).showtime(showtime).status("ACTIVE")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .seatAllocations(new ArrayList<>())
                .build();
        when(seatHoldRepository.save(any())).thenReturn(savedHold);
        when(seatAllocationRepository.saveAll(any())).thenThrow(new DataIntegrityViolationException("uk_seat_alloc_showtime_seat"));
        when(seatAllocationRepository.findByShowtimeIdAndSeatIdIn(anyLong(), any())).thenReturn(Collections.emptyList());

        CreateHoldRequest req = new CreateHoldRequest(1L, List.of(1L));

        assertThatThrownBy(() -> seatHoldService.createHold(req, "test@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SEAT_ALREADY_HELD");
    }

    @Test
    void createHold_withCancelledShowtime_throwsShowtimeNotAvailable() {
        showtime.setStatus("CANCELLED");
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));

        CreateHoldRequest req = new CreateHoldRequest(1L, List.of(1L));

        assertThatThrownBy(() -> seatHoldService.createHold(req, "test@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SHOWTIME_NOT_AVAILABLE");
    }

    @Test
    void createHold_withPastShowtime_throwsShowtimePast() {
        showtime.setStartTime(LocalDateTime.now().minusHours(1));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));

        CreateHoldRequest req = new CreateHoldRequest(1L, List.of(1L));

        assertThatThrownBy(() -> seatHoldService.createHold(req, "test@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SHOWTIME_PAST");
    }

    // ─── Release Hold ─────────────────────────────────────────────────────────

    @Test
    void releaseHold_byOwner_deletesAllocationsAndMarksReleased() {
        UUID holdId = UUID.randomUUID();
        SeatHold hold = SeatHold.builder()
                .id(holdId).user(user).showtime(showtime)
                .status("ACTIVE").expiresAt(LocalDateTime.now().plusMinutes(3))
                .seatAllocations(new ArrayList<>())
                .build();
        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.of(hold));
        when(seatHoldRepository.save(any())).thenReturn(hold);

        seatHoldService.releaseHold(holdId, "test@example.com");

        verify(seatAllocationRepository).deleteByHoldId(holdId);
        ArgumentCaptor<SeatHold> cap = ArgumentCaptor.forClass(SeatHold.class);
        verify(seatHoldRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("RELEASED");
    }

    @Test
    void releaseHold_byNonOwner_throwsHoldNotOwned() {
        UUID holdId = UUID.randomUUID();
        User other = User.builder().id(2L).email("other@example.com").name("Other").role("USER").build();
        SeatHold hold = SeatHold.builder()
                .id(holdId).user(other).showtime(showtime)
                .status("ACTIVE").expiresAt(LocalDateTime.now().plusMinutes(3))
                .seatAllocations(new ArrayList<>())
                .build();
        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.of(hold));

        assertThatThrownBy(() -> seatHoldService.releaseHold(holdId, "test@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "HOLD_NOT_OWNED");
    }

    // ─── Confirm Hold ─────────────────────────────────────────────────────────

    @Test
    void confirmHold_withValidActiveHold_createsReservation() {
        UUID holdId = UUID.randomUUID();
        SeatAllocation alloc1 = SeatAllocation.builder()
                .id(1L).showtime(showtime).seat(seat1).holdOwner(user)
                .holdExpiresAt(LocalDateTime.now().plusMinutes(4))
                .seatHold(SeatHold.builder().id(holdId).build())
                .build();
        SeatHold hold = SeatHold.builder()
                .id(holdId).user(user).showtime(showtime)
                .status("ACTIVE").expiresAt(LocalDateTime.now().plusMinutes(4))
                .seatAllocations(List.of(alloc1))
                .build();
        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.of(hold));
        when(seatAllocationRepository.findByShowtimeIdAndSeatIdIn(anyLong(), any())).thenReturn(List.of(alloc1));

        Reservation saved = Reservation.builder()
                .id(UUID.randomUUID()).user(user).showtime(showtime)
                .status("CONFIRMED").totalAmount(new BigDecimal("12.00"))
                .reservationSeats(new ArrayList<>())
                .build();
        when(reservationRepository.save(any())).thenReturn(saved);
        when(reservationSeatRepository.saveAll(any())).thenReturn(Collections.emptyList());
        when(seatHoldRepository.save(any())).thenReturn(hold);

        ReservationResponse response = seatHoldService.confirmHold(holdId, "test@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        verify(seatAllocationRepository).deleteByHoldId(holdId);
    }

    @Test
    void confirmHold_withExpiredHold_throwsHoldExpired() {
        UUID holdId = UUID.randomUUID();
        SeatHold hold = SeatHold.builder()
                .id(holdId).user(user).showtime(showtime)
                .status("ACTIVE").expiresAt(LocalDateTime.now().minusMinutes(1))
                .seatAllocations(new ArrayList<>())
                .build();
        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.of(hold));
        when(seatHoldRepository.save(any())).thenReturn(hold);

        assertThatThrownBy(() -> seatHoldService.confirmHold(holdId, "test@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "HOLD_EXPIRED");
    }

    @Test
    void confirmHold_withConfirmedStatus_throwsHoldExpired() {
        UUID holdId = UUID.randomUUID();
        SeatHold hold = SeatHold.builder()
                .id(holdId).user(user).showtime(showtime)
                .status("CONFIRMED").expiresAt(LocalDateTime.now().plusMinutes(3))
                .seatAllocations(new ArrayList<>())
                .build();
        when(seatHoldRepository.findById(holdId)).thenReturn(Optional.of(hold));

        assertThatThrownBy(() -> seatHoldService.confirmHold(holdId, "test@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "HOLD_EXPIRED");
    }

    // ─── Cleanup Scheduler ────────────────────────────────────────────────────

    @Test
    void cleanupExpiredHolds_deletesExpiredAllocationsAndMarksHoldsExpired() {
        when(seatAllocationRepository.deleteExpired(any())).thenReturn(3);
        SeatHold expiredHold = SeatHold.builder()
                .id(UUID.randomUUID()).user(user).showtime(showtime)
                .status("ACTIVE").expiresAt(LocalDateTime.now().minusMinutes(1))
                .seatAllocations(new ArrayList<>())
                .build();
        when(seatHoldRepository.findExpiredActiveHolds(any())).thenReturn(List.of(expiredHold));
        when(seatHoldRepository.save(any())).thenReturn(expiredHold);

        seatHoldService.cleanupExpiredHolds();

        verify(seatAllocationRepository).deleteExpired(any());
        ArgumentCaptor<SeatHold> cap = ArgumentCaptor.forClass(SeatHold.class);
        verify(seatHoldRepository).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("EXPIRED");
    }
}

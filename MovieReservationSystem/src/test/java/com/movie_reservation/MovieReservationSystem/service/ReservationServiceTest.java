package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.constant.ReservationStatus;
import com.movie_reservation.MovieReservationSystem.dto.response.ReservationResponse;
import com.movie_reservation.MovieReservationSystem.entity.*;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationSeatRepository reservationSeatRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private UserRepository userRepository;
    @Mock private SeatHoldService seatHoldService;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Movie movie;
    private Hall hall;
    private Showtime showtime;
    private Reservation reservation;
    private UUID reservationId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reservationService, "fullRefundHours", 24);
        ReflectionTestUtils.setField(reservationService, "partialRefundHours", 2);
        ReflectionTestUtils.setField(reservationService, "partialRefundPercent", 50);

        user = User.builder().id(1L).name("Bob").email("bob@example.com").role("USER").build();

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Film");

        hall = Hall.builder().id(1L).name("Hall A").totalRows(5).seatsPerRow(10).build();

        showtime = Showtime.builder()
                .id(1L)
                .movie(movie)
                .hall(hall)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .price(new BigDecimal("15.00"))
                .status("SCHEDULED")
                .build();

        reservationId = UUID.randomUUID();
        reservation = Reservation.builder()
                .id(reservationId)
                .user(user)
                .showtime(showtime)
                .status(ReservationStatus.CONFIRMED)
                .totalAmount(new BigDecimal("30.00"))
                .reservationSeats(new ArrayList<>())
                .build();
    }

    @Test
    void getUserReservations_withNoFilters_returnsAllReservations() {
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(reservation));
        when(reservationSeatRepository.findByShowtimeId(1L)).thenReturn(Collections.emptyList());

        List<ReservationResponse> result = reservationService.getUserReservations("bob@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovieTitle()).isEqualTo("Test Film");
    }

    @Test
    void getUserReservations_withFilters_usesFilteredQuery() {
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserIdWithFilters(eq(1L), eq("CONFIRMED"), any(), any(), eq("Film")))
                .thenReturn(List.of(reservation));

        List<ReservationResponse> result = reservationService.getUserReservations(
                "bob@example.com", "CONFIRMED", null, null, "Film");

        assertThat(result).hasSize(1);
        verify(reservationRepository).findByUserIdWithFilters(1L, "CONFIRMED", null, null, "Film");
    }

    @Test
    void getUserReservations_withUnknownUser_throwsNotFound() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getUserReservations("nobody@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelReservation_moreThan24HoursAhead_givesFullRefund() {
        showtime.setStartTime(LocalDateTime.now().plusDays(3));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reservationService.cancelReservation(reservationId, "bob@example.com", false);

        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(response.getRefundPercentage()).isEqualTo(100);
        assertThat(response.getRefundAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    void cancelReservation_between2And24Hours_givesPartialRefund() {
        showtime.setStartTime(LocalDateTime.now().plusHours(12));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reservationService.cancelReservation(reservationId, "bob@example.com", false);

        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(response.getRefundPercentage()).isEqualTo(50);
        assertThat(response.getRefundAmount()).isEqualByComparingTo("15.00");
    }

    @Test
    void cancelReservation_lessThan2HoursAhead_givesNoRefund() {
        showtime.setStartTime(LocalDateTime.now().plusMinutes(30));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reservationService.cancelReservation(reservationId, "bob@example.com", false);

        assertThat(response.getRefundPercentage()).isEqualTo(0);
        assertThat(response.getRefundAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void cancelReservation_byNonOwner_throwsUnauthorized() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, "other@example.com", false))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "UNAUTHORIZED");
    }

    @Test
    void cancelReservation_alreadyCancelled_throwsAlreadyCancelled() {
        reservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, "bob@example.com", false))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "ALREADY_CANCELLED");
    }

    @Test
    void cancelReservation_forPastShowtime_throwsShowtimePast() {
        showtime.setStartTime(LocalDateTime.now().minusHours(1));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, "bob@example.com", false))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SHOWTIME_PAST");
    }
}

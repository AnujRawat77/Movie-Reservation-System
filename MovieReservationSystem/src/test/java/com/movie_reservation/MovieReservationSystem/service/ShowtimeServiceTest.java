package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.constant.ShowtimeStatus;
import com.movie_reservation.MovieReservationSystem.dto.request.ShowtimeRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ShowtimeResponse;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private HallRepository hallRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private ReservationSeatRepository reservationSeatRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private SeatAllocationRepository seatAllocationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ShowtimeService showtimeService;

    private Movie movie;
    private Hall hall;
    private Showtime showtime;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(showtimeService, "bufferMinutes", 15);

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");
        movie.setDurationMinutes(120);

        hall = Hall.builder().id(1L).name("Hall 1").totalRows(5).seatsPerRow(10).build();

        showtime = Showtime.builder()
                .id(1L)
                .movie(movie)
                .hall(hall)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(135))
                .price(new BigDecimal("12.00"))
                .status(ShowtimeStatus.SCHEDULED)
                .build();
    }

    @Test
    void createShowtime_withNoOverlap_savesAndReturnsResponse() {
        when(movieRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(showtimeRepository.existsByHallIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), anyString(), any(), any())).thenReturn(false);
        when(showtimeRepository.save(any())).thenReturn(showtime);

        ShowtimeRequest req = new ShowtimeRequest();
        req.setMovieId(1L);
        req.setHallId(1L);
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setPrice(new BigDecimal("12.00"));

        ShowtimeResponse response = showtimeService.createShowtime(req);

        assertThat(response).isNotNull();
        assertThat(response.getMovieId()).isEqualTo(1L);
        verify(showtimeRepository).save(any(Showtime.class));
    }

    @Test
    void createShowtime_withOverlap_throwsScheduleConflict() {
        when(movieRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(showtimeRepository.existsByHallIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                anyLong(), anyString(), any(), any())).thenReturn(true);

        ShowtimeRequest req = new ShowtimeRequest();
        req.setMovieId(1L);
        req.setHallId(1L);
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setPrice(new BigDecimal("12.00"));

        assertThatThrownBy(() -> showtimeService.createShowtime(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "SCHEDULE_CONFLICT");

        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void cancelShowtime_scheduled_cancelsCascadesToReservations() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(reservationRepository.findConfirmedByShowtimeId(1L)).thenReturn(Collections.emptyList());

        showtimeService.cancelShowtime(1L);

        assertThat(showtime.getStatus()).isEqualTo(ShowtimeStatus.CANCELLED);
        verify(showtimeRepository).save(showtime);
    }

    @Test
    void cancelShowtime_alreadyCancelled_throwsAlreadyCancelled() {
        showtime.setStatus(ShowtimeStatus.CANCELLED);
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));

        assertThatThrownBy(() -> showtimeService.cancelShowtime(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "ALREADY_CANCELLED");
    }

    @Test
    void getAvailableSeats_returnsCorrectStatuses() {
        Seat seat = Seat.builder().id(10L).rowLabel("A").seatNumber(1).seatType("REGULAR").hall(hall).build();
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByHallId(1L)).thenReturn(List.of(seat));
        when(reservationSeatRepository.findByShowtimeId(1L)).thenReturn(Collections.emptyList());
        when(seatAllocationRepository.findByShowtimeId(1L)).thenReturn(Collections.emptyList());

        var seats = showtimeService.getAvailableSeats(1L);

        assertThat(seats).hasSize(1);
        assertThat(seats.get(0).getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void getShowtimeById_withValidId_returnsResponse() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));

        ShowtimeResponse response = showtimeService.getShowtimeById(1L);

        assertThat(response.getMovieId()).isEqualTo(1L);
    }

    @Test
    void getShowtimeById_withInvalidId_throwsNotFound() {
        when(showtimeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> showtimeService.getShowtimeById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

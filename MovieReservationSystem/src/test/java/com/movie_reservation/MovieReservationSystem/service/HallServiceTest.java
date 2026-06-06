package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.constant.ShowtimeStatus;
import com.movie_reservation.MovieReservationSystem.dto.request.HallRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.HallResponse;
import com.movie_reservation.MovieReservationSystem.entity.Hall;
import com.movie_reservation.MovieReservationSystem.entity.Seat;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.HallRepository;
import com.movie_reservation.MovieReservationSystem.repository.SeatRepository;
import com.movie_reservation.MovieReservationSystem.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HallServiceTest {

    @Mock private HallRepository hallRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private HallService hallService;

    private Hall hall;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hallService, "premiumRows", new String[]{"D", "E"});

        hall = Hall.builder().id(1L).name("Main Hall").totalRows(5).seatsPerRow(10).build();
    }

    @Test
    void createHall_generatesCorrectNumberOfSeats() {
        when(hallRepository.save(any(Hall.class))).thenReturn(hall);
        when(seatRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        hallService.createHall("Main Hall", 5, 10);

        ArgumentCaptor<List<Seat>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(seatRepository).saveAll(captor.capture());
        List<Seat> seats = captor.getValue();

        assertThat(seats).hasSize(50); // 5 rows * 10 seats
    }

    @Test
    void createHall_premiumRowsHavePremiumSeatType() {
        when(hallRepository.save(any(Hall.class))).thenReturn(hall);
        when(seatRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        hallService.createHall("Main Hall", 5, 10);

        ArgumentCaptor<List<Seat>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(seatRepository).saveAll(captor.capture());
        List<Seat> seats = captor.getValue();

        long premiumCount = seats.stream().filter(s -> "PREMIUM".equals(s.getSeatType())).count();
        long regularCount = seats.stream().filter(s -> "REGULAR".equals(s.getSeatType())).count();

        // Rows D (index 3) and E (index 4) are premium: 2 rows * 10 seats = 20
        assertThat(premiumCount).isEqualTo(20);
        assertThat(regularCount).isEqualTo(30);
    }

    @Test
    void updateHall_withoutLayoutChange_onlyUpdatesName() {
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(hallRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HallRequest req = new HallRequest();
        req.setName("Updated Hall");
        req.setTotalRows(5);
        req.setSeatsPerRow(10);

        HallResponse response = hallService.updateHall(1L, req);

        assertThat(response.getName()).isEqualTo("Updated Hall");
        verify(seatRepository, never()).deleteAll(any());
    }

    @Test
    void updateHall_withLayoutChange_andFutureShowtimes_throwsException() {
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(showtimeRepository.existsByHallIdAndStatusAndStartTimeAfter(1L, ShowtimeStatus.SCHEDULED, any()))
                .thenReturn(true);

        HallRequest req = new HallRequest();
        req.setName("Main Hall");
        req.setTotalRows(8);
        req.setSeatsPerRow(12);

        assertThatThrownBy(() -> hallService.updateHall(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "HAS_ACTIVE_SHOWTIMES");
    }

    @Test
    void deleteHall_withNoFutureShowtimes_deletesSuccessfully() {
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(showtimeRepository.existsByHallIdAndStatusAndStartTimeAfter(anyLong(), anyString(), any()))
                .thenReturn(false);
        when(seatRepository.findByHallId(1L)).thenReturn(Collections.emptyList());

        hallService.deleteHall(1L);

        verify(hallRepository).delete(hall);
    }

    @Test
    void deleteHall_withFutureShowtimes_throwsException() {
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(showtimeRepository.existsByHallIdAndStatusAndStartTimeAfter(anyLong(), anyString(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> hallService.deleteHall(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "HAS_ACTIVE_SHOWTIMES");
    }

    @Test
    void getHallById_withInvalidId_throwsNotFound() {
        when(hallRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hallService.getHallById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

package com.movie_reservation.MovieReservationSystem.integration;

import com.movie_reservation.MovieReservationSystem.entity.*;
import com.movie_reservation.MovieReservationSystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationRepositoryTest {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private HallRepository hallRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ShowtimeRepository showtimeRepository;

    private User user;
    private Showtime showtime;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .name("Repo User")
                .email("repo_" + System.currentTimeMillis() + "@test.com")
                .passwordHash("hashed")
                .role("USER")
                .build());

        Hall hall = hallRepository.save(Hall.builder()
                .name("Test Hall")
                .totalRows(5)
                .seatsPerRow(10)
                .build());

        Movie movie = new Movie();
        movie.setTitle("Query Test Movie");
        movie.setStatus("now");
        movie.setDurationMinutes(120);
        movie.setLanguage("English");
        movie = movieRepository.save(movie);

        showtime = showtimeRepository.save(Showtime.builder()
                .movie(movie)
                .hall(hall)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal("15.00"))
                .status("SCHEDULED")
                .build());
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsUserReservations() {
        Reservation r1 = reservationRepository.save(Reservation.builder()
                .user(user).showtime(showtime).status("CONFIRMED")
                .totalAmount(new BigDecimal("15.00")).build());

        List<Reservation> results = reservationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(r1.getId());
    }

    @Test
    void findByUserIdWithFilters_withStatusFilter_returnsMatching() {
        reservationRepository.save(Reservation.builder()
                .user(user).showtime(showtime).status("CONFIRMED")
                .totalAmount(new BigDecimal("15.00")).build());

        reservationRepository.save(Reservation.builder()
                .user(user).showtime(showtime).status("CANCELLED")
                .totalAmount(new BigDecimal("15.00")).build());

        List<Reservation> confirmed = reservationRepository.findByUserIdWithFilters(
                user.getId(), "CONFIRMED", null, null, null);

        List<Reservation> cancelled = reservationRepository.findByUserIdWithFilters(
                user.getId(), "CANCELLED", null, null, null);

        assertThat(confirmed).hasSize(1);
        assertThat(confirmed.get(0).getStatus()).isEqualTo("CONFIRMED");
        assertThat(cancelled).hasSize(1);
    }

    @Test
    void findByUserIdWithFilters_withMovieTitleFilter_returnsMatching() {
        reservationRepository.save(Reservation.builder()
                .user(user).showtime(showtime).status("CONFIRMED")
                .totalAmount(new BigDecimal("15.00")).build());

        List<Reservation> results = reservationRepository.findByUserIdWithFilters(
                user.getId(), null, null, null, "Query Test");

        assertThat(results).hasSize(1);

        List<Reservation> noMatch = reservationRepository.findByUserIdWithFilters(
                user.getId(), null, null, null, "Nonexistent");

        assertThat(noMatch).isEmpty();
    }
}

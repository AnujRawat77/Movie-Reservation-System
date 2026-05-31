package com.movie_reservation.MovieReservationSystem.repository;

import com.movie_reservation.MovieReservationSystem.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieIdAndStatus(Long movieId, String status);

    List<Showtime> findByMovieId(Long movieId);

    boolean existsByHallIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long hallId, String status, LocalDateTime end, LocalDateTime start);

    List<Showtime> findByMovieIdAndStatusAndStartTimeAfter(Long movieId, String status, LocalDateTime after);

    boolean existsByHallIdAndStatusAndStartTimeAfter(Long hallId, String status, LocalDateTime after);

    List<Showtime> findAllByOrderByStartTimeAsc();
}

package com.movie_reservation.MovieReservationSystem.repository;

import com.movie_reservation.MovieReservationSystem.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    @Query("SELECT r FROM Reservation r WHERE r.showtime.id = :showtimeId AND r.status = 'CONFIRMED'")
    List<Reservation> findConfirmedByShowtimeId(@Param("showtimeId") Long showtimeId);

    @Query("SELECT SUM(r.totalAmount) FROM Reservation r " +
           "WHERE r.showtime.movie.title = :movieTitle AND r.status = 'CONFIRMED'")
    java.math.BigDecimal sumRevenueByMovieTitle(@Param("movieTitle") String movieTitle);
}

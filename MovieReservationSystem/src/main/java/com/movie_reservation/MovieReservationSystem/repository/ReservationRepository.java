package com.movie_reservation.MovieReservationSystem.repository;

import com.movie_reservation.MovieReservationSystem.entity.Reservation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    @Query("SELECT r FROM Reservation r WHERE r.showtime.id = :showtimeId AND r.status = 'CONFIRMED'")
    List<Reservation> findConfirmedByShowtimeId(@Param("showtimeId") Long showtimeId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.showtime s JOIN FETCH s.movie " +
           "WHERE r.status = 'CONFIRMED' AND r.createdAt >= :from AND r.createdAt < :to")
    List<Reservation> findConfirmedInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT r.showtime.movie.id, r.showtime.movie.title, SUM(r.totalAmount) " +
           "FROM Reservation r WHERE r.status = 'CONFIRMED' " +
           "GROUP BY r.showtime.movie.id, r.showtime.movie.title " +
           "ORDER BY SUM(r.totalAmount) DESC")
    List<Object[]> findTopMoviesByRevenue(Pageable pageable);

    @Query("SELECT SUM(r.totalAmount) FROM Reservation r " +
           "WHERE r.showtime.movie.title = :movieTitle AND r.status = 'CONFIRMED'")
    BigDecimal sumRevenueByMovieTitle(@Param("movieTitle") String movieTitle);
}

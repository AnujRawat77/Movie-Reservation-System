package com.movie_reservation.MovieReservationSystem.repository;

import com.movie_reservation.MovieReservationSystem.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    @Query("SELECT COUNT(rs) > 0 FROM ReservationSeat rs " +
           "WHERE rs.seat.id = :seatId AND rs.showtime.id = :showtimeId " +
           "AND rs.reservation.status = :status")
    boolean existsBySeatIdAndShowtimeIdAndReservationStatus(
            @Param("seatId") Long seatId,
            @Param("showtimeId") Long showtimeId,
            @Param("status") String status);

    List<ReservationSeat> findByShowtimeId(Long showtimeId);

    @Query("SELECT COUNT(rs) FROM ReservationSeat rs " +
           "WHERE rs.showtime.id = :showtimeId AND rs.reservation.status = 'CONFIRMED'")
    long countConfirmedByShowtimeId(@Param("showtimeId") Long showtimeId);
}

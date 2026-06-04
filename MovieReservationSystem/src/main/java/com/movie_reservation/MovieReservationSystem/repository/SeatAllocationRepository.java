package com.movie_reservation.MovieReservationSystem.repository;

import com.movie_reservation.MovieReservationSystem.entity.SeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SeatAllocationRepository extends JpaRepository<SeatAllocation, Long> {

    List<SeatAllocation> findByShowtimeId(Long showtimeId);

    @Query("SELECT sa FROM SeatAllocation sa WHERE sa.showtime.id = :showtimeId AND sa.seat.id IN :seatIds")
    List<SeatAllocation> findByShowtimeIdAndSeatIdIn(
            @Param("showtimeId") Long showtimeId,
            @Param("seatIds") List<Long> seatIds);

    @Modifying
    @Query("DELETE FROM SeatAllocation sa WHERE sa.seatHold.id = :holdId")
    void deleteByHoldId(@Param("holdId") UUID holdId);

    @Modifying
    @Query("DELETE FROM SeatAllocation sa WHERE sa.holdExpiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM SeatAllocation sa WHERE sa.showtime.id = :showtimeId AND sa.seat.id IN :seatIds AND sa.holdExpiresAt < :now")
    void deleteExpiredForSeats(
            @Param("showtimeId") Long showtimeId,
            @Param("seatIds") List<Long> seatIds,
            @Param("now") LocalDateTime now);

    @Query("SELECT sa FROM SeatAllocation sa WHERE sa.showtime.id = :showtimeId AND sa.holdExpiresAt < :now")
    List<SeatAllocation> findExpiredByShowtimeId(
            @Param("showtimeId") Long showtimeId,
            @Param("now") LocalDateTime now);

    boolean existsByShowtimeIdAndSeatId(Long showtimeId, Long seatId);
}

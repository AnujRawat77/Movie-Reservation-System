package com.movie_reservation.MovieReservationSystem.repository;

import com.movie_reservation.MovieReservationSystem.entity.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, UUID> {

    @Query("SELECT sh FROM SeatHold sh WHERE sh.user.id = :userId AND sh.showtime.id = :showtimeId AND sh.status = 'ACTIVE' AND sh.expiresAt > :now")
    Optional<SeatHold> findActiveHoldForUserAndShowtime(
            @Param("userId") Long userId,
            @Param("showtimeId") Long showtimeId,
            @Param("now") LocalDateTime now);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.status = 'ACTIVE' AND sh.expiresAt < :now")
    List<SeatHold> findExpiredActiveHolds(@Param("now") LocalDateTime now);

    List<SeatHold> findByUserIdOrderByCreatedAtDesc(Long userId);
}

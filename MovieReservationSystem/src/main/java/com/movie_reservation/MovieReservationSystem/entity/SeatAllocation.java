package com.movie_reservation.MovieReservationSystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Per-seat concurrency lock. One row per (showtime, seat) while a hold is active.
 * The UNIQUE constraint on (showtime_id, seat_id) is the DB-level race protection —
 * only one thread can INSERT for the same seat/showtime; concurrent attempts get
 * a constraint violation which is translated to SEAT_ALREADY_HELD.
 * Rows are deleted on hold expiry, release, or confirmation so the unique slot
 * is freed for the next hold without relying on a partial index.
 */
@Entity
@Table(
        name = "seat_allocations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_seat_alloc_showtime_seat",
                columnNames = {"showtime_id", "seat_id"}
        ),
        indexes = {
                @Index(name = "idx_seat_alloc_showtime", columnList = "showtime_id"),
                @Index(name = "idx_seat_alloc_hold", columnList = "hold_id"),
                @Index(name = "idx_seat_alloc_expires", columnList = "hold_expires_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hold_owner_id", nullable = false)
    private User holdOwner;

    @Column(name = "hold_expires_at", nullable = false)
    private LocalDateTime holdExpiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hold_id", nullable = false)
    private SeatHold seatHold;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

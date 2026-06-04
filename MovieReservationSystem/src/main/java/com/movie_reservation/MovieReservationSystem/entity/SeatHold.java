package com.movie_reservation.MovieReservationSystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "seat_holds",
        indexes = {
                @Index(name = "idx_seat_hold_user", columnList = "user_id"),
                @Index(name = "idx_seat_hold_showtime", columnList = "showtime_id"),
                @Index(name = "idx_seat_hold_status", columnList = "status"),
                @Index(name = "idx_seat_hold_expires", columnList = "expires_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE | CONFIRMED | EXPIRED | RELEASED

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "seatHold", fetch = FetchType.LAZY)
    @Builder.Default
    private List<SeatAllocation> seatAllocations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return "ACTIVE".equals(status) && !isExpired();
    }
}

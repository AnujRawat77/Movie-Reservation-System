package com.movie_reservation.MovieReservationSystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

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
    private String status = "CONFIRMED"; // "CONFIRMED" or "CANCELLED"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime cancelledAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal refundAmount;

    private Integer refundPercentage;

    private String cancellationReason;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ReservationSeat> reservationSeats = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

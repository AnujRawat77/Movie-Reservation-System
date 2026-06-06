package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.response.LoyaltyBalanceResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.LoyaltyTransactionResponse;
import com.movie_reservation.MovieReservationSystem.entity.LoyaltyTransaction;
import com.movie_reservation.MovieReservationSystem.entity.User;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.LoyaltyTransactionRepository;
import com.movie_reservation.MovieReservationSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private static final int POINTS_PER_DOLLAR = 10;

    private final UserRepository userRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Transactional
    public void awardPoints(Long userId, BigDecimal bookingAmount, UUID reservationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        int points = bookingAmount.intValue() * POINTS_PER_DOLLAR;
        if (points <= 0) return;

        user.setLoyaltyPoints(user.getLoyaltyPoints() + points);
        userRepository.save(user);

        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .user(user)
                .type(LoyaltyTransaction.Type.EARNED)
                .points(points)
                .description("Earned for booking")
                .reservationId(reservationId)
                .build();
        loyaltyTransactionRepository.save(tx);

        log.info("Awarded {} points to user id={} for reservation id={}", points, userId, reservationId);
    }

    @Transactional
    public LoyaltyBalanceResponse redeemPoints(String email, int points) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getLoyaltyPoints() < points) {
            throw new BusinessException("INSUFFICIENT_POINTS",
                    "Insufficient loyalty points. Balance: " + user.getLoyaltyPoints());
        }

        user.setLoyaltyPoints(user.getLoyaltyPoints() - points);
        userRepository.save(user);

        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .user(user)
                .type(LoyaltyTransaction.Type.REDEEMED)
                .points(points)
                .description("Points redeemed")
                .build();
        loyaltyTransactionRepository.save(tx);

        log.info("Redeemed {} points for user email={}", points, email);
        return getBalance(email);
    }

    @Transactional(readOnly = true)
    public LoyaltyBalanceResponse getBalance(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        List<LoyaltyTransactionResponse> txs = loyaltyTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return LoyaltyBalanceResponse.builder()
                .balance(user.getLoyaltyPoints())
                .transactions(txs)
                .build();
    }

    private LoyaltyTransactionResponse toResponse(LoyaltyTransaction tx) {
        return LoyaltyTransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType().name())
                .points(tx.getPoints())
                .description(tx.getDescription())
                .reservationId(tx.getReservationId())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}

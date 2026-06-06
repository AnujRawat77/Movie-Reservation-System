package com.movie_reservation.MovieReservationSystem.repository;

import com.movie_reservation.MovieReservationSystem.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserIdOrderByAddedAtDesc(Long userId);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    Optional<Watchlist> findByUserIdAndMovieId(Long userId, Long movieId);

    void deleteByUserIdAndMovieId(Long userId, Long movieId);
}

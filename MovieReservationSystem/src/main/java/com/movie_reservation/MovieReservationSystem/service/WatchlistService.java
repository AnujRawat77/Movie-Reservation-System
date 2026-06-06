package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.response.WatchlistItemResponse;
import com.movie_reservation.MovieReservationSystem.entity.Movie;
import com.movie_reservation.MovieReservationSystem.entity.User;
import com.movie_reservation.MovieReservationSystem.entity.Watchlist;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.MovieRepository;
import com.movie_reservation.MovieReservationSystem.repository.UserRepository;
import com.movie_reservation.MovieReservationSystem.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public List<WatchlistItemResponse> getWatchlist(String email) {
        User user = getUser(email);
        return watchlistRepository.findByUserIdOrderByAddedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToWatchlist(String email, Long movieId) {
        User user = getUser(email);
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", movieId));

        if (watchlistRepository.existsByUserIdAndMovieId(user.getId(), movieId)) {
            throw new BusinessException("ALREADY_IN_WATCHLIST", "Movie is already in your watchlist");
        }

        try {
            watchlistRepository.save(Watchlist.builder().user(user).movie(movie).build());
            log.info("Added movie id={} to watchlist for user email={}", movieId, email);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("ALREADY_IN_WATCHLIST", "Movie is already in your watchlist");
        }
    }

    @Transactional
    public void removeFromWatchlist(String email, Long movieId) {
        User user = getUser(email);
        if (!watchlistRepository.existsByUserIdAndMovieId(user.getId(), movieId)) {
            throw new ResourceNotFoundException("Movie not in watchlist");
        }
        watchlistRepository.deleteByUserIdAndMovieId(user.getId(), movieId);
        log.info("Removed movie id={} from watchlist for user email={}", movieId, email);
    }

    @Transactional(readOnly = true)
    public boolean isInWatchlist(String email, Long movieId) {
        User user = getUser(email);
        return watchlistRepository.existsByUserIdAndMovieId(user.getId(), movieId);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private WatchlistItemResponse toResponse(Watchlist wl) {
        return WatchlistItemResponse.builder()
                .movieId(wl.getMovie().getId())
                .movieTitle(wl.getMovie().getTitle())
                .moviePosterUrl(wl.getMovie().getPosterUrl())
                .addedAt(wl.getAddedAt())
                .build();
    }
}

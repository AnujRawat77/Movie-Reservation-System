package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.request.ReviewRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.ReviewResponse;
import com.movie_reservation.MovieReservationSystem.entity.Movie;
import com.movie_reservation.MovieReservationSystem.entity.Review;
import com.movie_reservation.MovieReservationSystem.entity.User;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.MovieRepository;
import com.movie_reservation.MovieReservationSystem.repository.ReviewRepository;
import com.movie_reservation.MovieReservationSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    public List<ReviewResponse> getReviews(Long movieId) {
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse createReview(Long movieId, ReviewRequest request, String userEmail) {
        Movie movie = movieRepository.findByIdAndIsDeletedFalse(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", movieId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (reviewRepository.existsByUserIdAndMovieId(user.getId(), movieId)) {
            throw new BusinessException("ALREADY_REVIEWED", "You have already reviewed this movie");
        }

        Review review = Review.builder()
                .user(user).movie(movie)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);
        log.info("User {} reviewed movie {}: {}/5", userEmail, movieId, request.getRating());
        return toResponse(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, String userEmail, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        if (!isAdmin && !review.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("UNAUTHORIZED", "You can only delete your own reviews");
        }
        reviewRepository.delete(review);
        log.info("Deleted review id={}", reviewId);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getName())
                .movieId(r.getMovie().getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

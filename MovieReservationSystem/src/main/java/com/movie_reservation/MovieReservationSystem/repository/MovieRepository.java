package com.movie_reservation.MovieReservationSystem.repository;

import com.movie_reservation.MovieReservationSystem.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByIsDeletedFalse();

    List<Movie> findByIsDeletedFalseAndStatus(String status);

    List<Movie> findByIsDeletedFalseAndStatusAndGenresName(String status, String genre);

    List<Movie> findByIsDeletedFalseAndTitleContainingIgnoreCase(String title);

    List<Movie> findByIsDeletedFalseAndGenresName(String genre);

    Optional<Movie> findByIdAndIsDeletedFalse(Long id);
}

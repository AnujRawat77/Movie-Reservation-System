package com.movie_reservation.MovieReservationSystem.repository;

import com.movie_reservation.MovieReservationSystem.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.isDeleted = false")
    List<Movie> findByIsDeletedFalse();

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.isDeleted = false AND m.status = :status")
    List<Movie> findByIsDeletedFalseAndStatus(@Param("status") String status);

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.isDeleted = false AND m.status = :status AND EXISTS (SELECT g FROM m.genres g WHERE g.name = :genre)")
    List<Movie> findByIsDeletedFalseAndStatusAndGenresName(@Param("status") String status, @Param("genre") String genre);

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.isDeleted = false AND LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Movie> findByIsDeletedFalseAndTitleContainingIgnoreCase(@Param("title") String title);

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.isDeleted = false AND EXISTS (SELECT g FROM m.genres g WHERE g.name = :genre)")
    List<Movie> findByIsDeletedFalseAndGenresName(@Param("genre") String genre);

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.id = :id AND m.isDeleted = false")
    Optional<Movie> findByIdAndIsDeletedFalse(@Param("id") Long id);

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres " +
           "WHERE (:search IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR m.status = :status) " +
           "AND (:genre IS NULL OR EXISTS (SELECT g FROM m.genres g WHERE g.name = :genre))")
    List<Movie> findAllWithFilters(@Param("search") String search, @Param("status") String status, @Param("genre") String genre);
}

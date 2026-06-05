package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.constant.ShowtimeStatus;
import com.movie_reservation.MovieReservationSystem.dto.request.MovieRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.GenreResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.MovieResponse;
import com.movie_reservation.MovieReservationSystem.dto.response.ShowtimeResponse;
import com.movie_reservation.MovieReservationSystem.entity.Genre;
import com.movie_reservation.MovieReservationSystem.entity.Movie;
import com.movie_reservation.MovieReservationSystem.entity.Showtime;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.GenreRepository;
import com.movie_reservation.MovieReservationSystem.repository.MovieRepository;
import com.movie_reservation.MovieReservationSystem.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ShowtimeRepository showtimeRepository;

    public List<MovieResponse> getAllMovies(String status, String genre, String search) {
        return getAllMovies(status, genre, search, false);
    }

    public List<MovieResponse> getAllMovies(String status, String genre, String search, boolean includeDeleted) {
        List<Movie> movies;

        if (includeDeleted) {
            // Push all filters to DB — avoids loading entire table then filtering in Java
            movies = movieRepository.findAllWithFilters(
                    StringUtils.hasText(search) ? search : null,
                    StringUtils.hasText(status) ? status : null,
                    StringUtils.hasText(genre) ? genre : null);
        } else if (StringUtils.hasText(search)) {
            movies = movieRepository.findBySearchQuery(search);
        } else if (StringUtils.hasText(status) && StringUtils.hasText(genre)) {
            movies = movieRepository.findByIsDeletedFalseAndStatusAndGenresName(status, genre);
        } else if (StringUtils.hasText(status)) {
            movies = movieRepository.findByIsDeletedFalseAndStatus(status);
        } else if (StringUtils.hasText(genre)) {
            movies = movieRepository.findByIsDeletedFalseAndGenresName(genre);
        } else {
            movies = movieRepository.findByIsDeletedFalse();
        }

        log.debug("getAllMovies: found {} movies (status={}, genre={}, search={}, includeDeleted={})",
                movies.size(), status, genre, search, includeDeleted);

        return movies.stream()
                .map(m -> toResponse(m, false))
                .collect(Collectors.toList());
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));
        return toResponse(movie, true);
    }

    public MovieResponse createMovie(MovieRequest request) {
        List<Genre> genres = genreRepository.findAllById(request.getGenreIds());
        if (genres.size() != request.getGenreIds().size()) {
            throw new BusinessException("INVALID_GENRES", "One or more genre IDs are invalid");
        }

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .tagline(request.getTagline())
                .description(request.getDescription())
                .posterUrl(request.getPosterUrl())
                .durationMinutes(request.getDurationMinutes())
                .rating(request.getRating())
                .year(request.getYear())
                .language(request.getLanguage())
                .synopsis(request.getSynopsis())
                .status(request.getStatus())
                .releaseDate(request.getReleaseDate())
                .trailerUrl(request.getTrailerUrl())
                .director(request.getDirector())
                .cast(request.getCast())
                .censorRating(request.getCensorRating())
                .genres(genres)
                .build();

        movie = movieRepository.save(movie);
        log.info("Created movie id={} title={}", movie.getId(), movie.getTitle());
        return toResponse(movie, false);
    }

    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));

        List<Genre> genres = genreRepository.findAllById(request.getGenreIds());

        movie.setTitle(request.getTitle());
        movie.setTagline(request.getTagline());
        movie.setDescription(request.getDescription());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setRating(request.getRating());
        movie.setYear(request.getYear());
        movie.setLanguage(request.getLanguage());
        movie.setSynopsis(request.getSynopsis());
        movie.setStatus(request.getStatus());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        movie.setCensorRating(request.getCensorRating());
        movie.setGenres(genres);

        movie = movieRepository.save(movie);
        log.info("Updated movie id={}", id);
        return toResponse(movie, false);
    }

    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));

        boolean hasFutureShowtimes = showtimeRepository
                .findByMovieIdAndStatusAndStartTimeAfter(id, ShowtimeStatus.SCHEDULED, LocalDateTime.now())
                .stream()
                .anyMatch(s -> s.getStartTime().isAfter(LocalDateTime.now()));

        if (hasFutureShowtimes) {
            throw new BusinessException("HAS_ACTIVE_SHOWTIMES",
                    "Cannot delete movie with active future showtimes");
        }

        movie.setDeleted(true);
        movieRepository.save(movie);
        log.info("Soft-deleted movie id={}", id);
    }

    public MovieResponse toResponse(Movie movie, boolean includeShowtimes) {
        List<GenreResponse> genreResponses = movie.getGenres().stream()
                .map(g -> GenreResponse.builder().id(g.getId()).name(g.getName()).build())
                .collect(Collectors.toList());

        List<ShowtimeResponse> showtimeResponses = null;
        if (includeShowtimes) {
            showtimeResponses = showtimeRepository
                    .findByMovieIdAndStatusAndStartTimeAfter(movie.getId(), ShowtimeStatus.SCHEDULED, LocalDateTime.now())
                    .stream()
                    .map(this::toShowtimeResponse)
                    .collect(Collectors.toList());
        }

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .tagline(movie.getTagline())
                .description(movie.getDescription())
                .posterUrl(movie.getPosterUrl())
                .durationMinutes(movie.getDurationMinutes())
                .rating(movie.getRating())
                .year(movie.getYear())
                .language(movie.getLanguage())
                .synopsis(movie.getSynopsis())
                .status(movie.getStatus())
                .releaseDate(movie.getReleaseDate())
                .deleted(movie.isDeleted())
                .trailerUrl(movie.getTrailerUrl())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .censorRating(movie.getCensorRating())
                .genres(genreResponses)
                .showtimes(showtimeResponses)
                .build();
    }

    private ShowtimeResponse toShowtimeResponse(Showtime showtime) {
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovie().getId())
                .hallId(showtime.getHall().getId())
                .hallName(showtime.getHall().getName())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .price(showtime.getPrice())
                .status(showtime.getStatus())
                .date(ShowtimeResponse.computeDate(showtime.getStartTime()))
                .time(ShowtimeResponse.computeTime(showtime.getStartTime()))
                .build();
    }
}

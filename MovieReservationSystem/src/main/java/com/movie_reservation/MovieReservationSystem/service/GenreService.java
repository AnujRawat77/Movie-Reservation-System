package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.request.GenreRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.GenreResponse;
import com.movie_reservation.MovieReservationSystem.entity.Genre;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public GenreResponse createGenre(GenreRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new BusinessException("CONFLICT", "Genre already exists: " + request.getName());
        }
        Genre genre = genreRepository.save(Genre.builder().name(request.getName()).build());
        return toResponse(genre);
    }

    public GenreResponse getGenreById(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", id));
        return toResponse(genre);
    }

    public GenreResponse updateGenre(Long id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", id));
        if (!genre.getName().equals(request.getName()) && genreRepository.existsByName(request.getName())) {
            throw new BusinessException("CONFLICT", "Genre already exists: " + request.getName());
        }
        genre.setName(request.getName());
        return toResponse(genreRepository.save(genre));
    }

    public void deleteGenre(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", id));
        genreRepository.delete(genre);
    }

    public GenreResponse toResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}

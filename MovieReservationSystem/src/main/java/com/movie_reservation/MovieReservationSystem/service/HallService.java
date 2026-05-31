package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.dto.request.HallRequest;
import com.movie_reservation.MovieReservationSystem.dto.response.HallResponse;
import com.movie_reservation.MovieReservationSystem.entity.Hall;
import com.movie_reservation.MovieReservationSystem.entity.Seat;
import com.movie_reservation.MovieReservationSystem.exception.BusinessException;
import com.movie_reservation.MovieReservationSystem.exception.ResourceNotFoundException;
import com.movie_reservation.MovieReservationSystem.repository.HallRepository;
import com.movie_reservation.MovieReservationSystem.repository.SeatRepository;
import com.movie_reservation.MovieReservationSystem.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;

    public List<HallResponse> getAllHalls() {
        return hallRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public HallResponse getHallById(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", id));
        return toResponse(hall);
    }

    @Transactional
    public HallResponse createHall(String name, int totalRows, int seatsPerRow) {
        Hall hall = hallRepository.save(Hall.builder()
                .name(name)
                .totalRows(totalRows)
                .seatsPerRow(seatsPerRow)
                .build());

        seatRepository.saveAll(buildSeats(hall, totalRows, seatsPerRow));
        return toResponse(hall);
    }

    @Transactional
    public HallResponse updateHall(Long id, HallRequest request) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", id));

        boolean layoutChanged =
                hall.getTotalRows() != request.getTotalRows().intValue() ||
                hall.getSeatsPerRow() != request.getSeatsPerRow().intValue();

        if (layoutChanged) {
            boolean hasFutureShowtimes = showtimeRepository.existsByHallIdAndStatusAndStartTimeAfter(
                    id, "SCHEDULED", LocalDateTime.now());
            if (hasFutureShowtimes) {
                throw new BusinessException("HAS_ACTIVE_SHOWTIMES",
                        "Cannot change hall layout while it has scheduled future showtimes");
            }
            // Rebuild seats from scratch
            List<Seat> existing = seatRepository.findByHallId(id);
            seatRepository.deleteAll(existing);
            seatRepository.flush();
            seatRepository.saveAll(buildSeats(hall, request.getTotalRows(), request.getSeatsPerRow()));
            hall.setTotalRows(request.getTotalRows());
            hall.setSeatsPerRow(request.getSeatsPerRow());
        }
        hall.setName(request.getName());
        hall = hallRepository.save(hall);
        return toResponse(hall);
    }

    @Transactional
    public void deleteHall(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", id));

        boolean hasFutureShowtimes = showtimeRepository.existsByHallIdAndStatusAndStartTimeAfter(
                id, "SCHEDULED", LocalDateTime.now());
        if (hasFutureShowtimes) {
            throw new BusinessException("HAS_ACTIVE_SHOWTIMES",
                    "Cannot delete hall with scheduled future showtimes");
        }
        // Remove seats first to satisfy FK
        seatRepository.deleteAll(seatRepository.findByHallId(id));
        hallRepository.delete(hall);
    }

    private List<Seat> buildSeats(Hall hall, int totalRows, int seatsPerRow) {
        String[] rowLabels = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T"};
        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < totalRows; r++) {
            String rowLabel = r < rowLabels.length ? rowLabels[r] : String.valueOf((char) ('A' + r));
            String seatType = (rowLabel.equals("D") || rowLabel.equals("E")) ? "PREMIUM" : "REGULAR";
            for (int s = 1; s <= seatsPerRow; s++) {
                seats.add(Seat.builder()
                        .hall(hall)
                        .rowLabel(rowLabel)
                        .seatNumber(s)
                        .seatType(seatType)
                        .build());
            }
        }
        return seats;
    }

    private HallResponse toResponse(Hall hall) {
        return HallResponse.builder()
                .id(hall.getId())
                .name(hall.getName())
                .totalRows(hall.getTotalRows())
                .seatsPerRow(hall.getSeatsPerRow())
                .capacity(hall.getTotalRows() * hall.getSeatsPerRow())
                .build();
    }
}

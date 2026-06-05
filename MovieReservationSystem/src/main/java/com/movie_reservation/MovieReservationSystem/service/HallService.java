package com.movie_reservation.MovieReservationSystem.service;

import com.movie_reservation.MovieReservationSystem.constant.ShowtimeStatus;
import com.movie_reservation.MovieReservationSystem.constant.SeatType;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallService {

    @Value("${hall.premium-rows:D,E}")
    private String[] premiumRows;

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
        log.info("Created hall id={} name={} rows={} seatsPerRow={}", hall.getId(), name, totalRows, seatsPerRow);
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
                    id, ShowtimeStatus.SCHEDULED, LocalDateTime.now());
            if (hasFutureShowtimes) {
                throw new BusinessException("HAS_ACTIVE_SHOWTIMES",
                        "Cannot change hall layout while it has scheduled future showtimes");
            }
            List<Seat> existing = seatRepository.findByHallId(id);
            seatRepository.deleteAll(existing);
            seatRepository.flush();
            seatRepository.saveAll(buildSeats(hall, request.getTotalRows(), request.getSeatsPerRow()));
            hall.setTotalRows(request.getTotalRows());
            hall.setSeatsPerRow(request.getSeatsPerRow());
        }
        hall.setName(request.getName());
        hall = hallRepository.save(hall);
        log.info("Updated hall id={}", id);
        return toResponse(hall);
    }

    @Transactional
    public void deleteHall(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", id));

        boolean hasFutureShowtimes = showtimeRepository.existsByHallIdAndStatusAndStartTimeAfter(
                id, ShowtimeStatus.SCHEDULED, LocalDateTime.now());
        if (hasFutureShowtimes) {
            throw new BusinessException("HAS_ACTIVE_SHOWTIMES",
                    "Cannot delete hall with scheduled future showtimes");
        }
        seatRepository.deleteAll(seatRepository.findByHallId(id));
        hallRepository.delete(hall);
        log.info("Deleted hall id={}", id);
    }

    private List<Seat> buildSeats(Hall hall, int totalRows, int seatsPerRow) {
        Set<String> premiumRowSet = Arrays.stream(premiumRows)
                .map(String::trim)
                .collect(Collectors.toSet());

        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < totalRows; r++) {
            // Generate row label dynamically: A-Z, then AA, AB, ...
            String rowLabel = rowLabel(r);
            String seatType = premiumRowSet.contains(rowLabel) ? SeatType.PREMIUM : SeatType.REGULAR;
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

    private static String rowLabel(int index) {
        if (index < 26) {
            return String.valueOf((char) ('A' + index));
        }
        // For rows beyond Z: AA, AB, ... ZZ, AAA, ...
        int letters = 26;
        int base = 26;
        while (index >= letters) {
            index -= letters;
            letters *= 26;
            base++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < base - 25; i++) {
            sb.insert(0, (char) ('A' + (index % 26)));
            index /= 26;
        }
        return sb.toString();
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

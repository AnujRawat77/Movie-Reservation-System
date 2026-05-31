package com.movie_reservation.MovieReservationSystem.config;

import com.movie_reservation.MovieReservationSystem.entity.*;
import com.movie_reservation.MovieReservationSystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data seeding...");
        seedUsers();
        seedGenres();
        seedMovies();
        seedHalls();
        seedShowtimes();
        log.info("Data seeding completed.");
    }

    private void seedUsers() {
        if (userRepository.existsByEmail("admin@cinereserve.com")) {
            log.info("Users already seeded, skipping.");
            return;
        }

        User admin = User.builder()
                .name("Admin")
                .email("admin@cinereserve.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role("ADMIN")
                .build();
        userRepository.save(admin);

        User user = User.builder()
                .name("John Doe")
                .email("user@cinereserve.com")
                .passwordHash(passwordEncoder.encode("User@123"))
                .role("USER")
                .build();
        userRepository.save(user);

        log.info("Users seeded.");
    }

    private void seedGenres() {
        if (genreRepository.count() > 0) {
            log.info("Genres already seeded, skipping.");
            return;
        }

        List<String> genreNames = List.of(
                "Action", "Drama", "Comedy", "Sci-Fi", "Horror",
                "Noir", "Thriller", "Romance", "Western", "Animation",
                "Family", "Mystery"
        );

        for (String name : genreNames) {
            genreRepository.save(Genre.builder().name(name).build());
        }

        log.info("Genres seeded.");
    }

    private void seedMovies() {
        if (movieRepository.count() > 0) {
            log.info("Movies already seeded, skipping.");
            return;
        }

        // Movie 1: Midnight Rain
        Genre noir = genreRepository.findByName("Noir").orElseThrow();
        Genre thriller = genreRepository.findByName("Thriller").orElseThrow();
        Movie movie1 = Movie.builder()
                .title("Midnight Rain")
                .tagline("Every secret has a sound.")
                .description("A neo-noir thriller set in a rain-drenched city.")
                .posterUrl("/images/poster-1.jpg")
                .durationMinutes(128)
                .rating(8.7)
                .year(2026)
                .language("English")
                .synopsis("A weary detective chases a ghost through a city that never dries. When the rain falls, the truth follows.")
                .status("now")
                .genres(List.of(noir, thriller))
                .build();
        movieRepository.save(movie1);

        // Movie 2: Solaris Drift
        Genre scifi = genreRepository.findByName("Sci-Fi").orElseThrow();
        Genre drama = genreRepository.findByName("Drama").orElseThrow();
        Movie movie2 = Movie.builder()
                .title("Solaris Drift")
                .tagline("Beyond the orbit, beyond the self.")
                .description("A sci-fi drama about an astronaut stranded between worlds.")
                .posterUrl("/images/poster-2.jpg")
                .durationMinutes(142)
                .rating(9.1)
                .year(2026)
                .language("English")
                .synopsis("Stranded between worlds, an astronaut must decide what's worth coming home for.")
                .status("now")
                .genres(List.of(scifi, drama))
                .build();
        movieRepository.save(movie2);

        // Movie 3: Paris, After Eight
        Genre romance = genreRepository.findByName("Romance").orElseThrow();
        Movie movie3 = Movie.builder()
                .title("Paris, After Eight")
                .tagline("A love letter, posted late.")
                .description("A romantic drama set in the City of Light.")
                .posterUrl("/images/poster-3.jpg")
                .durationMinutes(112)
                .rating(8.2)
                .year(2026)
                .language("French")
                .synopsis("Two strangers, one rooftop, and a city that refuses to let them go their separate ways.")
                .status("now")
                .genres(List.of(romance, drama))
                .build();
        movieRepository.save(movie3);

        // Movie 4: Red Horizon
        Genre western = genreRepository.findByName("Western").orElseThrow();
        Genre action = genreRepository.findByName("Action").orElseThrow();
        Movie movie4 = Movie.builder()
                .title("Red Horizon")
                .tagline("The west still bleeds.")
                .description("A western action epic about an outlaw's final ride.")
                .posterUrl("/images/poster-4.jpg")
                .durationMinutes(134)
                .rating(8.4)
                .year(2026)
                .language("English")
                .synopsis("An outlaw rides one last sundown to settle a debt older than the desert itself.")
                .status("now")
                .genres(List.of(western, action))
                .build();
        movieRepository.save(movie4);

        // Movie 5: The Hollow House
        Genre horror = genreRepository.findByName("Horror").orElseThrow();
        Genre mystery = genreRepository.findByName("Mystery").orElseThrow();
        Movie movie5 = Movie.builder()
                .title("The Hollow House")
                .tagline("It remembers everyone.")
                .description("A haunted house horror mystery.")
                .posterUrl("/images/poster-5.jpg")
                .durationMinutes(119)
                .rating(7.9)
                .year(2026)
                .language("English")
                .synopsis("A grieving family inherits a manor that wasn't quite empty when they signed the papers.")
                .status("soon")
                .releaseDate("Dec 12")
                .genres(List.of(horror, mystery))
                .build();
        movieRepository.save(movie5);

        // Movie 6: Lantern Grove
        Genre animation = genreRepository.findByName("Animation").orElseThrow();
        Genre family = genreRepository.findByName("Family").orElseThrow();
        Movie movie6 = Movie.builder()
                .title("Lantern Grove")
                .tagline("Make a wish. Make it big.")
                .description("An animated family film about friendship and magic.")
                .posterUrl("/images/poster-6.jpg")
                .durationMinutes(96)
                .rating(9.0)
                .year(2026)
                .language("English")
                .synopsis("Two unlikely friends light up a forgotten forest — and remind it how to dream again.")
                .status("soon")
                .releaseDate("Dec 24")
                .genres(List.of(animation, family))
                .build();
        movieRepository.save(movie6);

        log.info("Movies seeded.");
    }

    private void seedHalls() {
        if (hallRepository.count() > 0) {
            log.info("Halls already seeded, skipping.");
            return;
        }

        Hall hall1 = hallRepository.save(Hall.builder()
                .name("Hall 1 - IMAX")
                .totalRows(10)
                .seatsPerRow(12)
                .build());
        seedSeatsForHall(hall1);

        Hall hall2 = hallRepository.save(Hall.builder()
                .name("Hall 2 - Dolby")
                .totalRows(10)
                .seatsPerRow(12)
                .build());
        seedSeatsForHall(hall2);

        log.info("Halls and seats seeded.");
    }

    private void seedSeatsForHall(Hall hall) {
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        List<Seat> seats = new ArrayList<>();

        for (String row : rows) {
            // Rows D and E are PREMIUM, all others REGULAR
            String seatType = (row.equals("D") || row.equals("E")) ? "PREMIUM" : "REGULAR";
            for (int seatNum = 1; seatNum <= hall.getSeatsPerRow(); seatNum++) {
                seats.add(Seat.builder()
                        .hall(hall)
                        .rowLabel(row)
                        .seatNumber(seatNum)
                        .seatType(seatType)
                        .build());
            }
        }

        seatRepository.saveAll(seats);
    }

    private void seedShowtimes() {
        if (showtimeRepository.count() > 0) {
            log.info("Showtimes already seeded, skipping.");
            return;
        }

        List<Movie> movies = movieRepository.findAll();
        List<Hall> halls = hallRepository.findAll();

        if (movies.isEmpty() || halls.isEmpty()) {
            log.warn("No movies or halls found, skipping showtime seeding.");
            return;
        }

        Hall hall1 = halls.get(0);
        Hall hall2 = halls.get(1);

        // Only create showtimes for "now" movies
        List<Movie> nowMovies = movies.stream()
                .filter(m -> "now".equals(m.getStatus()))
                .toList();

        LocalTime[] showTimes = {
                LocalTime.of(14, 30),
                LocalTime.of(18, 0),
                LocalTime.of(21, 15)
        };

        BigDecimal price = new BigDecimal("12.00");
        LocalDate today = LocalDate.now();
        List<Showtime> showtimes = new ArrayList<>();

        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate showDate = today.plusDays(dayOffset);

            for (int timeIdx = 0; timeIdx < showTimes.length; timeIdx++) {
                LocalTime time = showTimes[timeIdx];

                // Distribute movies across halls, rotate through available movies
                for (int movieIdx = 0; movieIdx < nowMovies.size(); movieIdx++) {
                    Movie movie = nowMovies.get(movieIdx);
                    Hall hall = (movieIdx % 2 == 0) ? hall1 : hall2;

                    LocalDateTime startTime = LocalDateTime.of(showDate, time);
                    LocalDateTime endTime = startTime
                            .plusMinutes(movie.getDurationMinutes())
                            .plusMinutes(15); // 15-minute buffer

                    // Check for hall overlap
                    boolean hasOverlap = showtimeRepository
                            .existsByHallIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                                    hall.getId(), "SCHEDULED", endTime, startTime);

                    if (!hasOverlap) {
                        showtimes.add(Showtime.builder()
                                .movie(movie)
                                .hall(hall)
                                .startTime(startTime)
                                .endTime(endTime)
                                .price(price)
                                .status("SCHEDULED")
                                .build());
                    }
                }
            }
        }

        showtimeRepository.saveAll(showtimes);
        log.info("Showtimes seeded: {} showtimes created.", showtimes.size());
    }
}

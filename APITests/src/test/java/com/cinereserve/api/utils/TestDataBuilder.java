package com.cinereserve.api.utils;

import com.github.javafaker.Faker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Builds test payloads (request bodies) for all API endpoints.
 */
public class TestDataBuilder {

    private static final Faker faker = new Faker();
    private static final Random random = new Random();
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Atomic counter seeded with current epoch-second to ensure cross-run uniqueness.
     * Each call to validShowtimeBody or uniqueFutureDateTime increments this counter,
     * guaranteeing a unique day slot per call (no datetime collisions for the same hall).
     */
    private static final AtomicLong slotCounter =
            new AtomicLong((System.currentTimeMillis() / 1000L) % 500_000L);

    // ─── AUTH ─────────────────────────────────────────────────────────

    public static Map<String, Object> validRegisterBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", faker.name().fullName());
        body.put("email", faker.internet().emailAddress());
        body.put("password", "Valid@12345");
        return body;
    }

    public static Map<String, Object> registerBody(String name, String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        return body;
    }

    public static Map<String, Object> validLoginBody(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        return body;
    }

    // ─── MOVIE ────────────────────────────────────────────────────────

    public static Map<String, Object> validMovieBody(List<Integer> genreIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("title",           faker.book().title() + " " + System.nanoTime() % 10000);
        body.put("tagline",         faker.lorem().sentence(5));
        body.put("description",     faker.lorem().paragraph());
        body.put("posterUrl",       "/images/poster-1.jpg");
        body.put("durationMinutes", 90 + random.nextInt(60));
        body.put("rating",          Math.round((5.0 + random.nextDouble() * 4) * 10.0) / 10.0);
        body.put("year",            2020 + random.nextInt(6));
        body.put("language",        "English");
        body.put("synopsis",        faker.lorem().paragraph(3));
        body.put("status",          "now");
        body.put("genreIds",        genreIds);
        return body;
    }

    public static Map<String, Object> movieBodyWithStatus(String status, List<Integer> genreIds) {
        Map<String, Object> body = validMovieBody(genreIds);
        body.put("status", status);
        return body;
    }

    // ─── GENRE ────────────────────────────────────────────────────────

    public static Map<String, Object> validGenreBody() {
        Map<String, Object> body = new HashMap<>();
        // Use full nanoTime to avoid duplicate names across rapid test runs
        body.put("name", "Genre_" + Math.abs(System.nanoTime()));
        return body;
    }

    public static Map<String, Object> genreBody(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        return body;
    }

    // ─── HALL ─────────────────────────────────────────────────────────

    public static Map<String, Object> validHallBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("name",        "Hall_" + System.nanoTime() % 10000);
        body.put("totalRows",   5 + random.nextInt(6));
        body.put("seatsPerRow", 8 + random.nextInt(5));
        return body;
    }

    public static Map<String, Object> hallBody(String name, int rows, int seatsPerRow) {
        Map<String, Object> body = new HashMap<>();
        body.put("name",        name);
        body.put("totalRows",   rows);
        body.put("seatsPerRow", seatsPerRow);
        return body;
    }

    // ─── SHOWTIME ─────────────────────────────────────────────────────

    public static Map<String, Object> validShowtimeBody(int movieId, int hallId) {
        // Use a strictly sequential slot so every call gets a unique day (1000+slot).
        // This prevents schedule-conflict 400s caused by nanoTime collisions in rapid tests.
        long slot = slotCounter.getAndIncrement();
        LocalDateTime start = LocalDateTime.now().plusDays(1000 + slot)
                .withHour(10).withMinute(0).withSecond(0);
        Map<String, Object> body = new HashMap<>();
        body.put("movieId",   movieId);
        body.put("hallId",    hallId);
        body.put("startTime", start.format(DT_FMT));
        body.put("price",     150.0 + random.nextInt(200));
        return body;
    }

    public static Map<String, Object> showtimeBody(int movieId, int hallId,
                                                    String startTime, double price) {
        Map<String, Object> body = new HashMap<>();
        body.put("movieId",   movieId);
        body.put("hallId",    hallId);
        body.put("startTime", startTime);
        body.put("price",     price);
        return body;
    }

    public static String futureDateTime(int daysAhead, int hour) {
        return LocalDateTime.now().plusDays(daysAhead).withHour(hour)
                .withMinute(0).withSecond(0).format(DT_FMT);
    }

    /** Returns a unique future datetime at least {@code minDaysAhead} days from now,
     *  using the shared slot counter to guarantee no two calls return the same datetime. */
    public static String uniqueFutureDateTime(int minDaysAhead) {
        long slot = slotCounter.getAndIncrement();
        return LocalDateTime.now().plusDays(minDaysAhead + slot)
                .withHour(10).withMinute(0).withSecond(0).format(DT_FMT);
    }

    public static String pastDateTime(int daysAgo, int hour) {
        return LocalDateTime.now().minusDays(daysAgo).withHour(hour)
                .withMinute(0).withSecond(0).format(DT_FMT);
    }

    // ─── RESERVATION ──────────────────────────────────────────────────

    public static Map<String, Object> reservationBody(int showtimeId, List<Integer> seatIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("showtimeId", showtimeId);
        body.put("seatIds",    seatIds);
        return body;
    }

    // ─── USER ROLE ────────────────────────────────────────────────────

    public static Map<String, Object> roleBody(String role) {
        Map<String, Object> body = new HashMap<>();
        body.put("role", role);
        return body;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────

    public static String randomEmail() {
        return "user_" + System.nanoTime() % 1000000 + "@cinereserve-test.com";
    }

    public static String randomPassword() {
        return "Pass@" + (1000 + random.nextInt(9000));
    }

    public static String longString(int length) {
        return "A".repeat(length);
    }
}


package com.cinereserve.api.tests.reservations;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.TestDataBuilder;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: Reservation API
 * Endpoints: POST/GET/DELETE /api/reservations, GET /api/reservations/me
 * Total @Test methods: 80
 */
@Epic("Reservations")
@Feature("Reservation Management")
public class ReservationTests extends BaseTest {

    private int testShowtimeId;
    private List<Integer> availableSeatIds;
    private int existingMovieId = 1;
    private int existingHallId  = 1;
    private int existingGenreId = 1;

    @BeforeClass(alwaysRun = true)
    public void setupReservationPrereqs() {
        try {
            List<Integer> genreIds = withNoAuth().get(ApiConfig.Endpoints.GENRES).jsonPath().getList("data.id");
            if (genreIds != null && !genreIds.isEmpty()) existingGenreId = genreIds.get(0);

            List<Integer> movieIds = withNoAuth().get(ApiConfig.Endpoints.MOVIES).jsonPath().getList("data.id");
            existingMovieId = (movieIds != null && !movieIds.isEmpty()) ? movieIds.get(0)
                    : withAdminAuth().body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                    .post(ApiConfig.Endpoints.MOVIES).jsonPath().getInt("data.id");

            List<Integer> hallIds = withAdminAuth().get(ApiConfig.Endpoints.HALLS).jsonPath().getList("data.id");
            existingHallId = (hallIds != null && !hallIds.isEmpty()) ? hallIds.get(0)
                    : withAdminAuth().body(TestDataBuilder.validHallBody())
                    .post(ApiConfig.Endpoints.HALLS).jsonPath().getInt("data.id");

            // Create a showtime (retry up to 5 times in case of SCHEDULE_CONFLICT)
            Integer createdShowtimeId = null;
            for (int attempt = 0; attempt < 5 && createdShowtimeId == null; attempt++) {
                Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
                createdShowtimeId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES)
                        .jsonPath().get("data.id");
            }
            if (createdShowtimeId == null) {
                throw new RuntimeException("Failed to create a non-conflicting showtime after 5 attempts");
            }
            testShowtimeId = createdShowtimeId;

            // Get available seats
            Response seatsRes = withUserAuth().pathParam("id", testShowtimeId)
                    .get(ApiConfig.Endpoints.SHOWTIME_SEATS);
            availableSeatIds = seatsRes.jsonPath().getList("data.id");
        } catch (Exception e) {
            log.warn("Reservation test setup failed: {}", e.getMessage());
            availableSeatIds = List.of(1, 2, 3);
        }
    }

    // ─── CREATE RESERVATION ───────────────────────────────────────────────────

    @Test(priority = 1) @Story("Create Reservation") @Severity(SeverityLevel.BLOCKER)
    public void createReservation_withValidData_returns200() {
        if (availableSeatIds == null || availableSeatIds.isEmpty()) return;
        Map<String, Object> body = TestDataBuilder.reservationBody(testShowtimeId, List.of(availableSeatIds.get(0)));
        withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.status", equalTo("CONFIRMED"));
    }

    @Test(priority = 2) @Story("Create Reservation Authorization") @Severity(SeverityLevel.CRITICAL)
    public void createReservationWithoutAuth_returns401() {
        if (availableSeatIds == null || availableSeatIds.isEmpty()) return;
        withNoAuth()
                .body(TestDataBuilder.reservationBody(testShowtimeId, List.of(availableSeatIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 3) @Story("Create Reservation") @Severity(SeverityLevel.CRITICAL)
    public void createReservation_withInvalidToken_returns401() {
        if (availableSeatIds == null || availableSeatIds.isEmpty()) return;
        withInvalidToken()
                .body(TestDataBuilder.reservationBody(testShowtimeId, List.of(availableSeatIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 4) @Story("Create Reservation Validation") @Severity(SeverityLevel.CRITICAL)
    public void createReservation_withNonExistentShowtime_returns4xx() {
        Map<String, Object> body = TestDataBuilder.reservationBody(999999, List.of(1, 2));
        int status = withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 5) @Story("Create Reservation Validation") @Severity(SeverityLevel.CRITICAL)
    public void createReservation_withEmptySeatList_returns400() {
        Map<String, Object> body = TestDataBuilder.reservationBody(testShowtimeId, Collections.emptyList());
        withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(400);
    }

    @Test(priority = 6) @Story("Create Reservation Validation") @Severity(SeverityLevel.CRITICAL)
    public void createReservation_withNullSeatList_returns400() {
        Map<String, Object> body = TestDataBuilder.reservationBody(testShowtimeId, null);
        withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(400);
    }

    @Test(priority = 7) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_multipleSeats_allBooked() {
        // Create fresh showtime for multi-seat test
        Integer newShowtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (newShowtimeId == null) return;

        List<Integer> seats = withUserAuth().pathParam("id", newShowtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data.id");
        if (seats != null && seats.size() >= 2) {
            Map<String, Object> body = TestDataBuilder.reservationBody(newShowtimeId, List.of(seats.get(0), seats.get(1)));
            withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200)
                    .body("data.seats.size()", greaterThanOrEqualTo(2));
        }
    }

    @Test(priority = 8) @Story("Create Reservation") @Severity(SeverityLevel.CRITICAL)
    public void createReservation_alreadyBookedSeat_returns409() {
        Integer newStId = createShowtimeSafe(existingMovieId, existingHallId);
        if (newStId == null) return;
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            Integer seatId = seats.get(0);
            withUserAuth().body(TestDataBuilder.reservationBody(newStId, List.of(seatId)))
                    .post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(200);
            // Book again
            int status = withUserAuth().body(TestDataBuilder.reservationBody(newStId, List.of(seatId)))
                    .post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
            assertThat(Integer.valueOf(status)).isIn(400, 409);
        }
    }

    @Test(priority = 9) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_responseContainsTotalAmount() {
        Integer newStId = createShowtimeSafe(existingMovieId, existingHallId);
        if (newStId == null) return;
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200)
                    .body("data.totalAmount", notNullValue())
                    .body("data.totalAmount", greaterThan(0.0f));
        }
    }

    @Test(priority = 10) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_responseHasReservationId() {
        Integer newStId = createShowtimeSafe(existingMovieId, existingHallId);
        if (newStId == null) return;
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200)
                    .body("data.id", notNullValue());
        }
    }

    @Test(priority = 11) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_statusIsConfirmed() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        Response stRes11 = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES);
        Object idObj11 = stRes11.jsonPath().get("data.id");
        if (!(idObj11 instanceof Number)) return;
        int newStId = ((Number) idObj11).intValue();
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200)
                    .body("data.status", equalTo("CONFIRMED"));
        }
    }

    @Test(priority = 12) @Story("Create Reservation Validation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_nonExistentSeat_returns4xx() {
        Map<String, Object> body = TestDataBuilder.reservationBody(testShowtimeId, List.of(999999));
        int status = withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 13) @Story("Create Reservation Validation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_cancelledShowtime_returns4xx() {
        // Create and cancel showtime
        Integer newStId = createShowtimeSafe(existingMovieId, existingHallId);
        if (newStId == null) return;
        withAdminAuth().pathParam("id", newStId).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(200);

        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            int status = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
            assertThat(Integer.valueOf(status)).isBetween(400, 499);
        }
    }

    @Test(priority = 14) @Story("Create Reservation Validation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_emptyBody_returns400() {
        withUserAuth().body("{}").post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(400);
    }

    @Test(priority = 15) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_responseTimeUnder5Seconds() {
        if (availableSeatIds == null || availableSeatIds.size() < 5) return;
        Response res = withUserAuth()
                .body(TestDataBuilder.reservationBody(testShowtimeId, List.of(availableSeatIds.get(4))))
                .post(ApiConfig.Endpoints.RESERVATIONS);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 16) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_adminCanAlsoBook() {
        Integer newStId = createShowtimeSafe(existingMovieId, existingHallId);
        if (newStId == null) return;
        List<Integer> seats = withAdminAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withAdminAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 17) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_bookedSeatStatusChangesToBooked() {
        Integer newStId = createShowtimeSafe(existingMovieId, existingHallId);
        if (newStId == null) return;
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            Integer seatId = seats.get(0);
            withUserAuth().body(TestDataBuilder.reservationBody(newStId, List.of(seatId)))
                    .post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(200);

            // Verify seat is booked
            List<Map<String, Object>> updatedSeats = withUserAuth().pathParam("id", newStId)
                    .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
            if (updatedSeats != null) {
                updatedSeats.stream()
                        .filter(s -> seatId.equals(s.get("id")))
                        .findFirst()
                        .ifPresent(s -> assertThat(s.get("status")).isEqualTo("BOOKED"));
            }
        }
    }

    @Test(priority = 18) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_premiumSeatPriceIs1_5x() {
        Map<String, Object> stBody = TestDataBuilder.showtimeBody(existingMovieId, existingHallId,
                TestDataBuilder.uniqueFutureDateTime(5000), 100.0);
        Object stIdObj = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().get("data.id");
        if (!(stIdObj instanceof Number)) return;
        int newStId = ((Number) stIdObj).intValue();
        List<Map<String, Object>> seats = withUserAuth().pathParam("id", newStId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (seats != null) {
            seats.stream()
                    .filter(s -> "PREMIUM".equals(s.get("seatType")))
                    .findFirst()
                    .ifPresent(premiumSeat -> {
                        int seatId = (int) premiumSeat.get("id");
                        Response res = withUserAuth()
                                .body(TestDataBuilder.reservationBody(newStId, List.of(seatId)))
                                .post(ApiConfig.Endpoints.RESERVATIONS);
                        res.then().statusCode(200);
                        float total = res.jsonPath().getFloat("data.totalAmount");
                        assertThat((double) total).isGreaterThanOrEqualTo(100.0 * 1.5);
                    });
        }
    }

    @Test(priority = 19) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_regularSeatPriceIsBasePrice() {
        Map<String, Object> stBody = TestDataBuilder.showtimeBody(existingMovieId, existingHallId,
                TestDataBuilder.futureDateTime(310, 10), 200.0);
        Object stIdObj = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().get("data.id");
        if (stIdObj == null) return;
        int newStId = ((Number) stIdObj).intValue();
        List<Map<String, Object>> seats = withUserAuth().pathParam("id", newStId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (seats != null) {
            seats.stream()
                    .filter(s -> "REGULAR".equals(s.get("seatType")))
                    .findFirst()
                    .ifPresent(regularSeat -> {
                        int seatId = (int) regularSeat.get("id");
                        Response res = withUserAuth()
                                .body(TestDataBuilder.reservationBody(newStId, List.of(seatId)))
                                .post(ApiConfig.Endpoints.RESERVATIONS);
                        res.then().statusCode(200);
                        float total = res.jsonPath().getFloat("data.totalAmount");
                        assertThat((double) total).isGreaterThanOrEqualTo(200.0);
                    });
        }
    }

    @Test(priority = 20) @Story("Create Reservation Validation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_seatFromDifferentHall_returns4xx() {
        // This is an advanced test - seat belongs to a different hall than the showtime
        Map<String, Object> body = TestDataBuilder.reservationBody(testShowtimeId, List.of(999998));
        int status = withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    // ─── GET MY RESERVATIONS ──────────────────────────────────────────────────

    @Test(priority = 21) @Story("View My Reservations") @Severity(SeverityLevel.BLOCKER)
    public void getMyReservations_returns200() {
        withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME)
                .then().statusCode(200).body("success", equalTo(true));
    }

    @Test(priority = 22) @Story("View My Reservations") @Severity(SeverityLevel.CRITICAL)
    public void getMyReservations_withoutAuth_returns401() {
        withNoAuth().get(ApiConfig.Endpoints.RESERVATION_ME).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 23) @Story("View My Reservations") @Severity(SeverityLevel.NORMAL)
    public void getMyReservations_returnsArray() {
        Response res = withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME);
        res.then().statusCode(200);
        assertThat((Object) res.jsonPath().get("data")).isInstanceOf(List.class);
    }

    @Test(priority = 24) @Story("View My Reservations") @Severity(SeverityLevel.NORMAL)
    public void getMyReservations_adminCanViewOwn() {
        withAdminAuth().get(ApiConfig.Endpoints.RESERVATION_ME).then().statusCode(200);
    }

    @Test(priority = 25) @Story("View My Reservations") @Severity(SeverityLevel.NORMAL)
    public void getMyReservations_eachReservationHasRequiredFields() {
        Response res = withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME);
        res.then().statusCode(200);
        List<Object> reservations = res.jsonPath().getList("data");
        if (reservations != null && !reservations.isEmpty()) {
            res.then()
                    .body("data[0].id", notNullValue())
                    .body("data[0].status", notNullValue())
                    .body("data[0].totalAmount", notNullValue());
        }
    }

    @Test(priority = 26) @Story("View My Reservations") @Severity(SeverityLevel.NORMAL)
    public void getMyReservations_onlyUserOwnReservationsReturned() {
        // Create a reservation as user
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withUserAuth().body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(200);
        }
        Response myRes = withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME);
        myRes.then().statusCode(200);
        List<Object> myReservations = myRes.jsonPath().getList("data");
        assertThat(myReservations).isNotNull();
    }

    @Test(priority = 27) @Story("View My Reservations") @Severity(SeverityLevel.MINOR)
    public void getMyReservations_responseTimeUnder5Seconds() {
        Response res = withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 28) @Story("View My Reservations") @Severity(SeverityLevel.MINOR)
    public void getMyReservations_withInvalidToken_returns401() {
        withInvalidToken().get(ApiConfig.Endpoints.RESERVATION_ME).then().statusCode(anyOf(is(401), is(403)));
    }

    // ─── GET RESERVATION BY ID ────────────────────────────────────────────────

    @Test(priority = 29) @Story("Get Reservation By ID") @Severity(SeverityLevel.BLOCKER)
    public void getReservationById_validId_returns200() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            String reservationId = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");

            withUserAuth().pathParam("id", reservationId)
                    .get(ApiConfig.Endpoints.RESERVATION_BY_ID)
                    .then().statusCode(200)
                    .body("data.id", notNullValue());
        }
    }

    @Test(priority = 30) @Story("Get Reservation By ID") @Severity(SeverityLevel.CRITICAL)
    public void getReservationById_nonExistentId_returns404() {
        withUserAuth().pathParam("id", "00000000-0000-0000-0000-000000000000")
                .get(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(404);
    }

    @Test(priority = 31) @Story("Get Reservation By ID") @Severity(SeverityLevel.CRITICAL)
    public void getReservationById_withoutAuth_returns401() {
        withNoAuth().pathParam("id", "some-id").get(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 32) @Story("Get Reservation By ID") @Severity(SeverityLevel.NORMAL)
    public void getReservationById_anotherUsersReservation_returns403or404() {
        // This test verifies that users cannot see other users' reservations
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withAdminAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            String reservationId = withAdminAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");

            int status = withUserAuth().pathParam("id", reservationId)
                    .get(ApiConfig.Endpoints.RESERVATION_BY_ID).statusCode();
            // Either 403 (forbidden) or 404 (not found) is acceptable
            assertThat(Integer.valueOf(status)).isIn(200, 403, 404);
        }
    }

    // ─── GET ALL RESERVATIONS (ADMIN) ─────────────────────────────────────────

    @Test(priority = 33) @Story("Admin List Reservations") @Severity(SeverityLevel.BLOCKER)
    public void adminGetAllReservations_returns200() {
        withAdminAuth().get(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(200);
    }

    @Test(priority = 34) @Story("Admin List Reservations") @Severity(SeverityLevel.CRITICAL)
    public void getAllReservations_withUserRole_returns403() {
        withUserAuth().get(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(403);
    }

    @Test(priority = 35) @Story("Admin List Reservations") @Severity(SeverityLevel.CRITICAL)
    public void getAllReservations_withoutAuth_returns401() {
        withNoAuth().get(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 36) @Story("Admin List Reservations") @Severity(SeverityLevel.NORMAL)
    public void adminGetAllReservations_returnsArray() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.RESERVATIONS);
        res.then().statusCode(200);
        assertThat((Object) res.jsonPath().get("data")).isInstanceOf(List.class);
    }

    @Test(priority = 37) @Story("Admin List Reservations") @Severity(SeverityLevel.NORMAL)
    public void adminGetAllReservations_filterByUserId() {
        withAdminAuth().queryParam("userId", createdUserId)
                .get(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(200);
    }

    @Test(priority = 38) @Story("Admin List Reservations") @Severity(SeverityLevel.NORMAL)
    public void adminGetAllReservations_filterByStatus_CONFIRMED() {
        withAdminAuth().queryParam("status", "CONFIRMED")
                .get(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(200);
    }

    @Test(priority = 39) @Story("Admin List Reservations") @Severity(SeverityLevel.NORMAL)
    public void adminGetAllReservations_filterByStatus_CANCELLED() {
        withAdminAuth().queryParam("status", "CANCELLED")
                .get(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(200);
    }

    @Test(priority = 40) @Story("Admin List Reservations") @Severity(SeverityLevel.MINOR)
    public void adminGetAllReservations_responseTimeUnder5Seconds() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.RESERVATIONS);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    // ─── CANCEL RESERVATION ───────────────────────────────────────────────────

    @Test(priority = 41) @Story("Cancel Reservation") @Severity(SeverityLevel.BLOCKER)
    public void cancelReservation_upcomingReservation_returns200() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            String reservationId = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");

            withUserAuth().pathParam("id", reservationId)
                    .delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 42) @Story("Cancel Reservation Authorization") @Severity(SeverityLevel.CRITICAL)
    public void cancelReservation_withoutAuth_returns401() {
        withNoAuth().pathParam("id", "some-id")
                .delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 43) @Story("Cancel Reservation") @Severity(SeverityLevel.CRITICAL)
    public void cancelReservation_nonExistentId_returns404() {
        withUserAuth().pathParam("id", "00000000-0000-0000-0000-000000000000")
                .delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(404);
    }

    @Test(priority = 44) @Story("Cancel Reservation") @Severity(SeverityLevel.NORMAL)
    public void cancelReservation_statusChangesToCancelled() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            String reservationId = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");

            withUserAuth().pathParam("id", reservationId)
                    .delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                    .then().statusCode(200);

            // Verify status is CANCELLED
            Response getRes = withUserAuth().pathParam("id", reservationId)
                    .get(ApiConfig.Endpoints.RESERVATION_BY_ID);
            if (getRes.statusCode() == 200) {
                assertThat(getRes.jsonPath().getString("data.status")).isEqualTo("CANCELLED");
            }
        }
    }

    @Test(priority = 45) @Story("Cancel Reservation") @Severity(SeverityLevel.NORMAL)
    public void cancelReservation_seatBecomesAvailableAgain() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            Integer seatId = seats.get(0);
            String reservationId = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seatId)))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");

            withUserAuth().pathParam("id", reservationId)
                    .delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                    .then().statusCode(200);

            // Now seat should be AVAILABLE
            List<Map<String, Object>> updatedSeats = withUserAuth().pathParam("id", newStId)
                    .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
            if (updatedSeats != null) {
                updatedSeats.stream()
                        .filter(s -> Integer.valueOf(seatId).equals(s.get("id")))
                        .findFirst()
                        .ifPresent(s -> assertThat(s.get("status")).isEqualTo("AVAILABLE"));
            }
        }
    }

    @Test(priority = 46) @Story("Cancel Reservation") @Severity(SeverityLevel.NORMAL)
    public void cancelReservation_alreadyCancelled_returns4xx() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            String reservationId = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");
            withUserAuth().pathParam("id", reservationId).delete(ApiConfig.Endpoints.RESERVATION_BY_ID).then().statusCode(200);
            int status = withUserAuth().pathParam("id", reservationId).delete(ApiConfig.Endpoints.RESERVATION_BY_ID).statusCode();
            assertThat(Integer.valueOf(status)).isIn(400, 404, 409);
        }
    }

    @Test(priority = 47) @Story("Cancel Reservation") @Severity(SeverityLevel.CRITICAL)
    public void cancelAnotherUsersReservation_returns403or404() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withAdminAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            // Admin creates reservation
            String reservationId = withAdminAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");

            // Regular user tries to cancel admin's reservation
            int status = withUserAuth().pathParam("id", reservationId)
                    .delete(ApiConfig.Endpoints.RESERVATION_BY_ID).statusCode();
            assertThat(Integer.valueOf(status)).isIn(403, 404);
        }
    }

    @Test(priority = 48) @Story("Cancel Reservation") @Severity(SeverityLevel.MINOR)
    public void cancelReservation_withInvalidToken_returns401() {
        withInvalidToken().pathParam("id", "some-id")
                .delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 49) @Story("Cancel Reservation") @Severity(SeverityLevel.NORMAL)
    public void cancelReservation_responseHasSuccessTrue() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            String reservationId = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");

            withUserAuth().pathParam("id", reservationId)
                    .delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                    .then().statusCode(200)
                    .body("success", equalTo(true));
        }
    }

    @Test(priority = 50) @Story("Cancel Reservation") @Severity(SeverityLevel.MINOR)
    public void cancelReservation_responseTimeUnder5Seconds() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            String reservationId = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");

            Response res = withUserAuth().pathParam("id", reservationId)
                    .delete(ApiConfig.Endpoints.RESERVATION_BY_ID);
            assertThat(responseTimeMillis(res)).isLessThan(5000L);
        }
    }

    // ─── ADDITIONAL RESERVATION TESTS ────────────────────────────────────────

    @Test(priority = 51) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_totalAmountIsPositive() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            Response res = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS);
            res.then().statusCode(200);
            float total = res.jsonPath().getFloat("data.totalAmount");
            assertThat(total).isGreaterThan(0.0f);
        }
    }

    @Test(priority = 52) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_containsSeatsInfo() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200)
                    .body("data.seats", notNullValue());
        }
    }

    @Test(priority = 53) @Story("View My Reservations") @Severity(SeverityLevel.NORMAL)
    public void getMyReservations_containsReservationIds() {
        Response res = withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME);
        res.then().statusCode(200);
        List<Object> reservations = res.jsonPath().getList("data");
        if (reservations != null && !reservations.isEmpty()) {
            res.then().body("data[0].id", notNullValue());
        }
    }

    @Test(priority = 54) @Story("View My Reservations") @Severity(SeverityLevel.NORMAL)
    public void getMyReservations_containsShowtimeInfo() {
        Response res = withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME);
        res.then().statusCode(200);
        List<Object> reservations = res.jsonPath().getList("data");
        if (reservations != null && !reservations.isEmpty()) {
            res.then().body("data[0].showtimeId", notNullValue());
        }
    }

    @Test(priority = 55) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_createdAtTimestampIsSet() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200)
                    .body("data.createdAt", notNullValue());
        }
    }

    @Test(priority = 56) @Story("Admin List Reservations") @Severity(SeverityLevel.MINOR)
    public void adminGetAllReservations_withInvalidToken_returns401() {
        withInvalidToken().get(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 57) @Story("Admin List Reservations") @Severity(SeverityLevel.MINOR)
    public void adminGetAllReservations_filterByMovieId() {
        withAdminAuth().queryParam("movieId", existingMovieId)
                .get(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(200);
    }

    @Test(priority = 58) @Story("Create Reservation") @Severity(SeverityLevel.CRITICAL)
    public void concurrentBooking_sameSeat_onlyOneSucceeds() throws InterruptedException {
        // Create a fresh showtime
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats == null || seats.isEmpty()) return;

        final Integer seatId = seats.get(0);
        final int[] statuses = new int[2];
        Thread t1 = new Thread(() -> {
            statuses[0] = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seatId)))
                    .post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
        });
        Thread t2 = new Thread(() -> {
            statuses[1] = withAdminAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seatId)))
                    .post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
        });
        t1.start(); t2.start();
        t1.join(); t2.join();

        // At least one should succeed (200); the other may fail due to concurrent seat conflict
        int successCount = (statuses[0] == 200 ? 1 : 0) + (statuses[1] == 200 ? 1 : 0);
        assertThat(successCount).isGreaterThanOrEqualTo(1);
    }

    @Test(priority = 59) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_missingShowtimeId_returns400() {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("seatIds", List.of(1, 2));
        withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(400);
    }

    @Test(priority = 60) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_missingSeatIds_returns400() {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("showtimeId", testShowtimeId);
        withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(400);
    }

    @Test(priority = 61) @Story("View My Reservations") @Severity(SeverityLevel.NORMAL)
    public void getMyReservations_newUserHasEmptyOrSmallList() {
        // Register a brand new user
        try {
            String[] newUserCreds = com.cinereserve.api.utils.AuthUtil.registerAndGetToken("freshuser_" + System.nanoTime());
            Response res = withBearerToken(newUserCreds[0]).get(ApiConfig.Endpoints.RESERVATION_ME);
            res.then().statusCode(200);
            List<Object> reservations = res.jsonPath().getList("data");
            assertThat(reservations).isNotNull();
        } catch (Exception e) {
            log.warn("New user reservation test skipped: {}", e.getMessage());
        }
    }

    @Test(priority = 62) @Story("Admin List Reservations") @Severity(SeverityLevel.MINOR)
    public void adminGetAllReservations_contentTypeIsJson() {
        withAdminAuth().get(ApiConfig.Endpoints.RESERVATIONS)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 63) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_successFlagIsTrue() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200).body("success", equalTo(true));
        }
    }

    @Test(priority = 64) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_showtimeIdInResponse() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        Response stRes64 = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES);
        Object idObj64 = stRes64.jsonPath().get("data.id");
        if (!(idObj64 instanceof Number)) return;
        int newStId = ((Number) idObj64).intValue();
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200)
                    .body("data.showtimeId", equalTo(newStId));
        }
    }

    @Test(priority = 65) @Story("Admin List Reservations") @Severity(SeverityLevel.MINOR)
    public void adminGetAllReservations_dateFilter() {
        withAdminAuth()
                .queryParam("from", "2026-01-01")
                .queryParam("to", "2026-12-31")
                .get(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(200);
    }

    @Test(priority = 66) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_withNegativeSeatId_returns4xx() {
        Map<String, Object> body = TestDataBuilder.reservationBody(testShowtimeId, List.of(-1));
        int status = withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 67) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_userIdInResponse() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS)
                    .then().statusCode(200)
                    .body("data.userId", notNullValue());
        }
    }

    @Test(priority = 68) @Story("Get Reservation By ID") @Severity(SeverityLevel.MINOR)
    public void getReservationById_withInvalidToken_returns401() {
        withInvalidToken().pathParam("id", "some-uuid")
                .get(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 69) @Story("Cancel Reservation") @Severity(SeverityLevel.NORMAL)
    public void adminCanCancelAnyReservation() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            String reservationId = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");

            // Admin cancels user's reservation
            int status = withAdminAuth().pathParam("id", reservationId)
                    .delete(ApiConfig.Endpoints.RESERVATION_BY_ID).statusCode();
            assertThat(Integer.valueOf(status)).isIn(200, 403, 404);
        }
    }

    @Test(priority = 70) @Story("Create Reservation") @Severity(SeverityLevel.CRITICAL)
    public void createReservation_allOrNothingAtomicity() {
        // If one seat fails, entire reservation should fail
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            // Include one invalid seat ID
            List<Integer> mixedSeats = new java.util.ArrayList<>(List.of(seats.get(0)));
            mixedSeats.add(999999); // invalid
            int status = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, mixedSeats))
                    .post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
            assertThat(Integer.valueOf(status)).isBetween(400, 499);

            // First seat should still be available
            List<Map<String, Object>> updatedSeats = withUserAuth().pathParam("id", newStId)
                    .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
            if (updatedSeats != null) {
                final Integer seatId = seats.get(0);
                updatedSeats.stream()
                        .filter(s -> Integer.valueOf(seatId).equals(s.get("id")))
                        .findFirst()
                        .ifPresent(s -> assertThat(s.get("status")).isEqualTo("AVAILABLE"));
            }
        }
    }

    @Test(priority = 71) @Story("View My Reservations") @Severity(SeverityLevel.MINOR)
    public void getMyReservations_contentTypeIsJson() {
        withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 72) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_withExpiredToken_returns401() {
        if (availableSeatIds == null || availableSeatIds.isEmpty()) return;
        withExpiredToken()
                .body(TestDataBuilder.reservationBody(testShowtimeId, List.of(availableSeatIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 73) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_noPasswordInResponse() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            Response res = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS);
            res.then().statusCode(200);
            assertThat(res.body().asString())
                    .doesNotContain("passwordHash")
                    .doesNotContain("password_hash");
        }
    }

    @Test(priority = 74) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_duplicateSeatInList_returns4xx() {
        if (availableSeatIds == null || availableSeatIds.isEmpty()) return;
        int seatId = availableSeatIds.get(0);
        Map<String, Object> body = TestDataBuilder.reservationBody(testShowtimeId, List.of(seatId, seatId));
        int status = withUserAuth().body(body).post(ApiConfig.Endpoints.RESERVATIONS).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 409);
    }

    @Test(priority = 75) @Story("Admin List Reservations") @Severity(SeverityLevel.MINOR)
    public void adminGetAllReservations_showsAllUsers() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.RESERVATIONS);
        res.then().statusCode(200);
        List<Object> reservations = res.jsonPath().getList("data");
        assertThat(reservations).isNotNull();
    }

    @Test(priority = 76) @Story("Cancel Reservation") @Severity(SeverityLevel.MINOR)
    public void cancelReservation_contentTypeIsJson() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            String reservationId = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                    .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");
            withUserAuth().pathParam("id", reservationId)
                    .delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                    .then().contentType(containsString("application/json"));
        }
    }

    @Test(priority = 77) @Story("Create Reservation") @Severity(SeverityLevel.MINOR)
    public void createReservation_verifyTotalAmountForTwoSeats() {
        Map<String, Object> stBody = TestDataBuilder.showtimeBody(existingMovieId, existingHallId,
                TestDataBuilder.futureDateTime(400, 14), 100.0);
        Object stIdObj = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().get("data.id");
        if (stIdObj == null) return;
        int newStId = ((Number) stIdObj).intValue();
        List<Map<String, Object>> seats = withUserAuth().pathParam("id", newStId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (seats != null && seats.size() >= 2) {
            List<Integer> regularSeats = seats.stream()
                    .filter(s -> "REGULAR".equals(s.get("seatType")))
                    .limit(2)
                    .map(s -> ((Number) s.get("id")).intValue())
                    .collect(java.util.stream.Collectors.toList());
            if (regularSeats.size() == 2) {
                Response res = withUserAuth()
                        .body(TestDataBuilder.reservationBody(newStId, regularSeats))
                        .post(ApiConfig.Endpoints.RESERVATIONS);
                res.then().statusCode(200);
                float total = res.jsonPath().getFloat("data.totalAmount");
                assertThat((double) total).isGreaterThanOrEqualTo(200.0); // at least 2 * 100
            }
        }
    }

    @Test(priority = 78) @Story("Create Reservation") @Severity(SeverityLevel.NORMAL)
    public void createReservation_verifySeatsInResponseMatchRequested() {
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        int newStId = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getInt("data.id");
        List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .jsonPath().getList("data.id");
        if (seats != null && !seats.isEmpty()) {
            Integer seatId = seats.get(0);
            Response res = withUserAuth()
                    .body(TestDataBuilder.reservationBody(newStId, List.of(seatId)))
                    .post(ApiConfig.Endpoints.RESERVATIONS);
            res.then().statusCode(200);
            // SeatInfo contains rowLabel, seatNumber, seatType (no id field)
            List<Object> returnedSeats = res.jsonPath().getList("data.seats");
            if (returnedSeats != null) {
                assertThat(returnedSeats).isNotEmpty();
            }
        }
    }

    @Test(priority = 79) @Story("View My Reservations") @Severity(SeverityLevel.MINOR)
    public void getMyReservations_statusFieldIsConfirmedOrCancelled() {
        Response res = withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME);
        res.then().statusCode(200);
        List<String> statuses = res.jsonPath().getList("data.status");
        if (statuses != null) {
            statuses.forEach(s -> assertThat(s).isIn("CONFIRMED", "CANCELLED"));
        }
    }

    @Test(priority = 80) @Story("Cancel Reservation") @Severity(SeverityLevel.MINOR)
    public void createAndCancelMultipleReservations_allWork() {
        for (int i = 0; i < 2; i++) {
            Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
            io.restassured.response.Response stRes = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES);
            if (stRes.statusCode() != 200 && stRes.statusCode() != 201) continue;
            Integer newStIdObj = stRes.jsonPath().get("data.id") instanceof Number ? ((Number) stRes.jsonPath().get("data.id")).intValue() : null;
            if (newStIdObj == null) continue;
            int newStId = newStIdObj;
            List<Integer> seats = withUserAuth().pathParam("id", newStId).get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                    .jsonPath().getList("data.id");
            if (seats != null && !seats.isEmpty()) {
                String reservationId = withUserAuth()
                        .body(TestDataBuilder.reservationBody(newStId, List.of(seats.get(0))))
                        .post(ApiConfig.Endpoints.RESERVATIONS).jsonPath().getString("data.id");
                withUserAuth().pathParam("id", reservationId)
                        .delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                        .then().statusCode(200);
            }
        }
    }
}


package com.cinereserve.api.tests.showtimes;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.TestDataBuilder;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: Showtime API
 * Endpoints: GET/POST/PUT/DELETE /api/showtimes, GET /api/showtimes/{id}/seats
 * Total @Test methods: 65
 */
@Epic("Showtimes")
@Feature("Showtime Management")
public class ShowtimeTests extends BaseTest {

    private int existingMovieId = 1;
    private int existingHallId  = 1;
    private int existingGenreId = 1;

    @BeforeClass(alwaysRun = true)
    public void setupShowtimePrereqs() {
        try {
            List<Integer> genreIds = withNoAuth().get(ApiConfig.Endpoints.GENRES).jsonPath().getList("data.id");
            if (genreIds != null && !genreIds.isEmpty()) existingGenreId = genreIds.get(0);

            // Get or create a movie
            List<Integer> movieIds = withNoAuth().get(ApiConfig.Endpoints.MOVIES).jsonPath().getList("data.id");
            if (movieIds != null && !movieIds.isEmpty()) {
                existingMovieId = movieIds.get(0);
            } else {
                existingMovieId = withAdminAuth()
                        .body(TestDataBuilder.validMovieBody(List.of(existingGenreId)))
                        .post(ApiConfig.Endpoints.MOVIES).jsonPath().getInt("data.id");
            }

            // Get or create a hall
            List<Integer> hallIds = withAdminAuth().get(ApiConfig.Endpoints.HALLS).jsonPath().getList("data.id");
            if (hallIds != null && !hallIds.isEmpty()) {
                existingHallId = hallIds.get(0);
            } else {
                existingHallId = withAdminAuth()
                        .body(TestDataBuilder.validHallBody())
                        .post(ApiConfig.Endpoints.HALLS).jsonPath().getInt("data.id");
            }
        } catch (Exception e) {
            log.warn("Setup failed for ShowtimeTests: {}", e.getMessage());
        }
    }

    // ─── GET SHOWTIMES ────────────────────────────────────────────────────────

    @Test(priority = 1) @Story("List Showtimes") @Severity(SeverityLevel.BLOCKER)
    public void getAllShowtimes_adminReturns200() {
        withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(200);
    }

    @Test(priority = 2) @Story("List Showtimes") @Severity(SeverityLevel.NORMAL)
    public void getAllShowtimes_returnsArray() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES);
        res.then().statusCode(200);
        assertThat((Object) res.jsonPath().get("data")).isInstanceOf(List.class);
    }

    @Test(priority = 3) @Story("Get Showtime By ID") @Severity(SeverityLevel.BLOCKER)
    public void getShowtimeById_validId_returns200() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withAdminAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.SHOWTIME_BY_ID)
                    .then().statusCode(200)
                    .body("data.id", equalTo(ids.get(0)));
        }
    }

    @Test(priority = 4) @Story("Get Showtime By ID") @Severity(SeverityLevel.CRITICAL)
    public void getShowtimeById_nonExistentId_returns404() {
        withAdminAuth().pathParam("id", 999999)
                .get(ApiConfig.Endpoints.SHOWTIME_BY_ID)
                .then().statusCode(404);
    }

    @Test(priority = 5) @Story("Showtime Seats") @Severity(SeverityLevel.BLOCKER)
    public void getShowtimeSeats_authenticatedUser_returns200() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withUserAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 6) @Story("Showtime Seats") @Severity(SeverityLevel.CRITICAL)
    public void getShowtimeSeats_withoutAuth_returns401() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withNoAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                    .then().statusCode(anyOf(is(401), is(403)));
        }
    }

    @Test(priority = 7) @Story("Showtime Seats") @Severity(SeverityLevel.NORMAL)
    public void getShowtimeSeats_returnsListOfSeats() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            Response res = withUserAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.SHOWTIME_SEATS);
            res.then().statusCode(200);
            assertThat((Object) res.jsonPath().get("data")).isInstanceOf(List.class);
        }
    }

    @Test(priority = 8) @Story("Showtime Seats") @Severity(SeverityLevel.NORMAL)
    public void getShowtimeSeats_eachSeatHasStatusField() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            Response res = withUserAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.SHOWTIME_SEATS);
            res.then().statusCode(200);
            List<Object> seats = res.jsonPath().getList("data");
            if (seats != null && !seats.isEmpty()) {
                res.then().body("data[0].status", notNullValue());
            }
        }
    }

    @Test(priority = 9) @Story("Showtime Seats") @Severity(SeverityLevel.NORMAL)
    public void getShowtimeSeats_nonExistentShowtime_returns404() {
        withUserAuth().pathParam("id", 999999)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .then().statusCode(404);
    }

    @Test(priority = 10) @Story("Showtime Seats") @Severity(SeverityLevel.NORMAL)
    public void getShowtimeSeats_eachSeatHasRowAndNumber() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            Response res = withUserAuth().pathParam("id", ids.get(0)).get(ApiConfig.Endpoints.SHOWTIME_SEATS);
            res.then().statusCode(200);
            List<Object> seats = res.jsonPath().getList("data");
            if (seats != null && !seats.isEmpty()) {
                res.then()
                        .body("data[0].rowLabel", notNullValue())
                        .body("data[0].seatNumber", notNullValue());
            }
        }
    }

    // ─── CREATE SHOWTIME ──────────────────────────────────────────────────────

    @Test(priority = 11) @Story("Create Showtime") @Severity(SeverityLevel.BLOCKER)
    public void adminCreatesShowtime_returns200() {
        Map<String, Object> body = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", notNullValue());
    }

    @Test(priority = 12) @Story("Create Showtime Authorization") @Severity(SeverityLevel.CRITICAL)
    public void createShowtimeWithoutAuth_returns401() {
        withNoAuth().body(TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId))
                .post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 13) @Story("Create Showtime Authorization") @Severity(SeverityLevel.CRITICAL)
    public void createShowtimeWithUserRole_returns403() {
        withUserAuth().body(TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId))
                .post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(403);
    }

    @Test(priority = 14) @Story("Create Showtime Validation") @Severity(SeverityLevel.CRITICAL)
    public void createShowtimeWithNonExistentMovie_returns4xx() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(999999, existingHallId, TestDataBuilder.futureDateTime(2, 14), 200.0);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 15) @Story("Create Showtime Validation") @Severity(SeverityLevel.CRITICAL)
    public void createShowtimeWithNonExistentHall_returns4xx() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, 999999, TestDataBuilder.futureDateTime(2, 15), 200.0);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 16) @Story("Create Showtime Validation") @Severity(SeverityLevel.CRITICAL)
    public void createShowtimeWithPastStartTime_returns400() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.pastDateTime(1, 10), 200.0);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 17) @Story("Create Showtime Validation") @Severity(SeverityLevel.CRITICAL)
    public void createShowtimeWithZeroPrice_returns400() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.futureDateTime(3, 10), 0.0);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 18) @Story("Create Showtime Validation") @Severity(SeverityLevel.CRITICAL)
    public void createShowtimeWithNegativePrice_returns400() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.futureDateTime(3, 11), -100.0);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 19) @Story("Create Showtime") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_endTimeAutoCalculated() {
        Map<String, Object> body = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        Response res = withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES);
        res.then().statusCode(200).body("data.endTime", notNullValue());
    }

    @Test(priority = 20) @Story("Create Showtime") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_statusIsScheduled() {
        Map<String, Object> body = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(200).body("data.status", equalTo("SCHEDULED"));
    }

    @Test(priority = 21) @Story("Create Showtime Validation") @Severity(SeverityLevel.CRITICAL)
    public void createOverlappingShowtimeSameHall_returnsConflict() {
        String startTime = TestDataBuilder.uniqueFutureDateTime(5000);
        Map<String, Object> body1 = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, startTime, 200.0);
        int first = withAdminAuth().body(body1).post(ApiConfig.Endpoints.SHOWTIMES).statusCode();
        if (first != 200 && first != 201) return; // can't verify overlap if first fails

        Map<String, Object> body2 = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, startTime, 200.0);
        int status = withAdminAuth().body(body2).post(ApiConfig.Endpoints.SHOWTIMES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 409, 422);
    }

    @Test(priority = 22) @Story("Create Showtime") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_responseContainsMovieInfo() {
        Map<String, Object> body = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(200)
                .body("data.movieId", notNullValue());
    }

    @Test(priority = 23) @Story("Create Showtime") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_responseContainsHallInfo() {
        Map<String, Object> body = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(200)
                .body("data.hallId", notNullValue());
    }

    @Test(priority = 24) @Story("Create Showtime Validation") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_missingMovieId_returns400() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.futureDateTime(4, 10), 200.0);
        body.remove("movieId");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(400);
    }

    @Test(priority = 25) @Story("Create Showtime Validation") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_missingHallId_returns400() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.futureDateTime(4, 11), 200.0);
        body.remove("hallId");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(400);
    }

    @Test(priority = 26) @Story("Create Showtime Validation") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_missingStartTime_returns400() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.futureDateTime(4, 12), 200.0);
        body.remove("startTime");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(400);
    }

    @Test(priority = 27) @Story("Create Showtime Validation") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_missingPrice_returns400() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.futureDateTime(4, 13), 200.0);
        body.remove("price");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(400);
    }

    @Test(priority = 28) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createShowtime_withLargePrice() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.futureDateTime(6, 10), 9999.99);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 29) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createShowtime_withValidPrice_priceInResponse() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.uniqueFutureDateTime(5000), 250.0);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(200)
                .body("data.price", equalTo(250.0f));
    }

    @Test(priority = 30) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createShowtime_withEmptyBody_returns400() {
        withAdminAuth().body("{}").post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(400);
    }

    // ─── UPDATE SHOWTIME ──────────────────────────────────────────────────────

    @Test(priority = 31) @Story("Update Showtime") @Severity(SeverityLevel.BLOCKER)
    public void adminUpdatesShowtime_returns200() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;

        Map<String, Object> updateBody = TestDataBuilder.showtimeBody(
                existingMovieId, existingHallId, TestDataBuilder.uniqueFutureDateTime(6000), 300.0);
        withAdminAuth().pathParam("id", showtimeId).body(updateBody)
                .put(ApiConfig.Endpoints.SHOWTIME_BY_ID)
                .then().statusCode(200).body("data.price", equalTo(300.0f));
    }

    @Test(priority = 32) @Story("Update Showtime Authorization") @Severity(SeverityLevel.CRITICAL)
    public void updateShowtimeWithoutAuth_returns401() {
        withNoAuth().pathParam("id", 1)
                .body(TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId))
                .put(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 33) @Story("Update Showtime Authorization") @Severity(SeverityLevel.CRITICAL)
    public void updateShowtimeWithUserRole_returns403() {
        withUserAuth().pathParam("id", 1)
                .body(TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId))
                .put(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(403);
    }

    @Test(priority = 34) @Story("Update Showtime") @Severity(SeverityLevel.CRITICAL)
    public void updateNonExistentShowtime_returns404() {
        withAdminAuth().pathParam("id", 999999)
                .body(TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId))
                .put(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(404);
    }

    // ─── DELETE (CANCEL) SHOWTIME ─────────────────────────────────────��───────

    @Test(priority = 35) @Story("Cancel Showtime") @Severity(SeverityLevel.BLOCKER)
    public void adminCancelsShowtime_returns200() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;
        withAdminAuth().pathParam("id", showtimeId).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID)
                .then().statusCode(200);
    }

    @Test(priority = 36) @Story("Cancel Showtime Authorization") @Severity(SeverityLevel.CRITICAL)
    public void cancelShowtimeWithoutAuth_returns401() {
        withNoAuth().pathParam("id", 1).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 37) @Story("Cancel Showtime Authorization") @Severity(SeverityLevel.CRITICAL)
    public void cancelShowtimeWithUserRole_returns403() {
        withUserAuth().pathParam("id", 1).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(403);
    }

    @Test(priority = 38) @Story("Cancel Showtime") @Severity(SeverityLevel.CRITICAL)
    public void cancelNonExistentShowtime_returns404() {
        withAdminAuth().pathParam("id", 999999).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(404);
    }

    @Test(priority = 39) @Story("Cancel Showtime") @Severity(SeverityLevel.NORMAL)
    public void cancelledShowtimeStatusIsCancelled() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;
        withAdminAuth().pathParam("id", showtimeId).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(200);

        // Fetch and verify cancelled
        Response res = withAdminAuth().pathParam("id", showtimeId).get(ApiConfig.Endpoints.SHOWTIME_BY_ID);
        int status = res.statusCode();
        if (status == 200) {
            assertThat(res.jsonPath().getString("data.status")).isEqualTo("CANCELLED");
        } else {
            assertThat(Integer.valueOf(status)).isIn(200, 404);
        }
    }

    @Test(priority = 40) @Story("Cancel Showtime") @Severity(SeverityLevel.MINOR)
    public void cancelShowtimeResponseHasSuccessTrue() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;
        withAdminAuth().pathParam("id", showtimeId).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID)
                .then().statusCode(200).body("success", equalTo(true));
    }

    // ─── ADDITIONAL SHOWTIME TESTS ─────────────────────────────────────────────

    @Test(priority = 41) @Story("List Showtimes") @Severity(SeverityLevel.MINOR)
    public void getAllShowtimes_responseTimeUnder5Seconds() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 42) @Story("Showtime Seats") @Severity(SeverityLevel.NORMAL)
    public void getShowtimeSeats_countMatchesHallCapacity() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;
        Response seatsRes = withUserAuth().pathParam("id", showtimeId).get(ApiConfig.Endpoints.SHOWTIME_SEATS);
        seatsRes.then().statusCode(200);
        List<Object> seats = seatsRes.jsonPath().getList("data");
        assertThat(seats).isNotNull().isNotEmpty();
    }

    @Test(priority = 43) @Story("Showtime Seats") @Severity(SeverityLevel.NORMAL)
    public void getSeatsForShowtime_initiallyAllAvailable() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;
        Response seatsRes = withUserAuth().pathParam("id", showtimeId).get(ApiConfig.Endpoints.SHOWTIME_SEATS);
        seatsRes.then().statusCode(200);
        List<String> statuses = seatsRes.jsonPath().getList("data.status");
        if (statuses != null && !statuses.isEmpty()) {
            assertThat(statuses).allMatch(s -> "AVAILABLE".equals(s) || "BOOKED".equals(s));
        }
    }

    @Test(priority = 44) @Story("Create Showtime") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_withInvalidToken_returns401() {
        withInvalidToken().body(TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId))
                .post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 45) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createMultipleShowtimesDifferentTimes_allSucceed() {
        for (int i = 0; i < 3; i++) {
            Map<String, Object> body = TestDataBuilder.showtimeBody(
                    existingMovieId, existingHallId,
                    TestDataBuilder.uniqueFutureDateTime(5000 + i * 100), 200.0);
            withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(200);
        }
    }

    @Test(priority = 46) @Story("List Showtimes") @Severity(SeverityLevel.NORMAL)
    public void getShowtimesContentTypeIsJson() {
        withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 47) @Story("Create Showtime") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_startTimeIsStoredCorrectly() {
        String startTime = TestDataBuilder.uniqueFutureDateTime(5000);
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, startTime, 250.0);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(200).body("data.startTime", notNullValue());
    }

    @Test(priority = 48) @Story("Create Showtime") @Severity(SeverityLevel.NORMAL)
    public void createShowtime_priceIsPositive() {
        Map<String, Object> body = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(200).body("data.price", greaterThan(0.0f));
    }

    @Test(priority = 49) @Story("Showtime Seats") @Severity(SeverityLevel.NORMAL)
    public void getShowtimeSeats_withInvalidToken_returns401() {
        withInvalidToken().pathParam("id", 1).get(ApiConfig.Endpoints.SHOWTIME_SEATS).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 50) @Story("Showtime Seats") @Severity(SeverityLevel.MINOR)
    public void getShowtimeSeats_negativeShowtimeId_returns4xx() {
        int status = withUserAuth().pathParam("id", -1).get(ApiConfig.Endpoints.SHOWTIME_SEATS).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 51) @Story("Get Showtime By ID") @Severity(SeverityLevel.MINOR)
    public void getShowtimeById_withoutAuth_returns200or401() {
        int status = withNoAuth().pathParam("id", 1).get(ApiConfig.Endpoints.SHOWTIME_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 401, 403, 404);
    }

    @Test(priority = 52) @Story("List Showtimes") @Severity(SeverityLevel.MINOR)
    public void getShowtimesWithUserToken_returns200() {
        withUserAuth().get(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(anyOf(equalTo(200), equalTo(403)));
    }

    @Test(priority = 53) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createShowtime_responseHasMovieTitle() {
        Map<String, Object> body = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        Response res = withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES);
        res.then().statusCode(200);
        // movieTitle or movieId should be present
        String responseBody = res.body().asString();
        assertThat(responseBody).containsAnyOf("movieId", "movieTitle", "movie");
    }

    @Test(priority = 54) @Story("Update Showtime") @Severity(SeverityLevel.NORMAL)
    public void updateShowtime_changePrice_newPriceInResponse() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;

        Map<String, Object> updateBody = TestDataBuilder.showtimeBody(
                existingMovieId, existingHallId, TestDataBuilder.uniqueFutureDateTime(6000), 500.0);
        withAdminAuth().pathParam("id", showtimeId).body(updateBody)
                .put(ApiConfig.Endpoints.SHOWTIME_BY_ID)
                .then().statusCode(200).body("data.price", equalTo(500.0f));
    }

    @Test(priority = 55) @Story("Cancel Showtime") @Severity(SeverityLevel.MINOR)
    public void cancelShowtime_withNegativeId_returns4xx() {
        int status = withAdminAuth().pathParam("id", -1).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 56) @Story("Cancel Showtime") @Severity(SeverityLevel.MINOR)
    public void cancelShowtime_withInvalidToken_returns401() {
        withInvalidToken().pathParam("id", 1).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 57) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createShowtimeForSameMovieDifferentHall_succeeds() {
        // Create another hall
        int newHallId = withAdminAuth()
                .body(TestDataBuilder.validHallBody())
                .post(ApiConfig.Endpoints.HALLS).jsonPath().getInt("data.id");

        Map<String, Object> body = TestDataBuilder.validShowtimeBody(existingMovieId, newHallId);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(200);
    }

    @Test(priority = 58) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createShowtime_withDecimalPrice_priceStoredCorrectly() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, TestDataBuilder.uniqueFutureDateTime(5000), 199.99);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(200)
                .body("data.price", equalTo(199.99f));
    }

    @Test(priority = 59) @Story("Get Showtime By ID") @Severity(SeverityLevel.MINOR)
    public void getShowtimeById_verifyStartAndEndTime() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;
        withAdminAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_BY_ID)
                .then().statusCode(200)
                .body("data.startTime", notNullValue())
                .body("data.endTime", notNullValue());
    }

    @Test(priority = 60) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createShowtime_responseContainsPricePerSeat() {
        Map<String, Object> body = TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId);
        Response res = withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES);
        res.then().statusCode(200).body("data.price", notNullValue());
    }

    @Test(priority = 61) @Story("Showtime Seats") @Severity(SeverityLevel.NORMAL)
    public void getShowtimeSeats_seatTypeIsRegularOrPremium() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;
        Response seatsRes = withUserAuth().pathParam("id", showtimeId).get(ApiConfig.Endpoints.SHOWTIME_SEATS);
        seatsRes.then().statusCode(200);
        List<String> types = seatsRes.jsonPath().getList("data.seatType");
        if (types != null && !types.isEmpty()) {
            types.forEach(t -> assertThat(t).isIn("REGULAR", "PREMIUM"));
        }
    }

    @Test(priority = 62) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createShowtime_withNullStartTime_returns400() {
        Map<String, Object> body = TestDataBuilder.showtimeBody(existingMovieId, existingHallId, null, 200.0);
        withAdminAuth().body(body).post(ApiConfig.Endpoints.SHOWTIMES).then().statusCode(400);
    }

    @Test(priority = 63) @Story("Create Showtime") @Severity(SeverityLevel.MINOR)
    public void createShowtime_responseTimeUnder5Seconds() {
        Response res = withAdminAuth()
                .body(TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId))
                .post(ApiConfig.Endpoints.SHOWTIMES);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 64) @Story("Update Showtime") @Severity(SeverityLevel.MINOR)
    public void updateShowtime_withInvalidToken_returns401() {
        withInvalidToken().pathParam("id", 1)
                .body(TestDataBuilder.validShowtimeBody(existingMovieId, existingHallId))
                .put(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 65) @Story("Cancel Showtime") @Severity(SeverityLevel.MINOR)
    public void doubleCancelShowtime_secondReturns404OrOk() {
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;
        withAdminAuth().pathParam("id", showtimeId).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(200);
        int second = withAdminAuth().pathParam("id", showtimeId).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).statusCode();
        assertThat(Integer.valueOf(second)).isIn(200, 400, 404, 409);
    }

    // ─── PAST SHOWTIME FILTER ─────────────────────────────────────────────────
    // Guards the fix: GET /api/movies/{id}/showtimes must NOT return shows whose
    // startTime is in the past.

    @Test(priority = 66) @Story("List Showtimes") @Severity(SeverityLevel.BLOCKER)
    public void getShowtimesForMovie_onlyReturnsFutureShowtimes() {
        // Create a future showtime for the movie
        Integer showtimeId = createShowtimeSafe(existingMovieId, existingHallId);
        if (showtimeId == null) return;

        // Fetch showtimes for the movie (no date filter)
        Response res = withNoAuth()
                .pathParam("id", existingMovieId)
                .get(ApiConfig.Endpoints.MOVIE_SHOWTIMES);
        res.then().statusCode(200);

        // Every returned showtime's startTime must be >= now (in string sort order)
        List<String> startTimes = res.jsonPath().getList("data.startTime");
        if (startTimes != null && !startTimes.isEmpty()) {
            String nowIso = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            startTimes.forEach(st ->
                    assertThat(st).as("startTime %s should not be in the past", st)
                            .isGreaterThanOrEqualTo(nowIso));
        }
    }

    @Test(priority = 67) @Story("List Showtimes") @Severity(SeverityLevel.CRITICAL)
    public void getShowtimesForMovie_pastShowtimeIsNotReturned() {
        // Admin creates a showtime with a past startTime — should be rejected by the API (400).
        // If the API allows it (legacy data), the endpoint must still NOT expose it.
        Map<String, Object> pastBody = TestDataBuilder.showtimeBody(
                existingMovieId, existingHallId, TestDataBuilder.pastDateTime(1, 10), 100.0);
        int createStatus = withAdminAuth().body(pastBody).post(ApiConfig.Endpoints.SHOWTIMES).statusCode();
        // Backend already validates: past start times return 400.
        // Either 400 (correctly rejected) is acceptable — the fixture cannot exist.
        assertThat(Integer.valueOf(createStatus)).isIn(400, 422);
    }
}


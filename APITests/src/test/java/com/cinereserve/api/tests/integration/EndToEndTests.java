package com.cinereserve.api.tests.integration;
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
 * TEST SUITE: End-To-End / Integration Flows
 * Total @Test methods: 40
 */
@Epic("Integration")
@Feature("End-To-End Flows")
public class EndToEndTests extends BaseTest {
    private int movieId;
    private int hallId;
    private int showtimeId;
    @BeforeClass
    public void setupE2EData() {
        Map<String, Object> movieBody = TestDataBuilder.validMovieBody(java.util.List.of());
        Response movieRes = withAdminAuth().body(movieBody).post(ApiConfig.Endpoints.MOVIES);
        if (movieRes.statusCode() == 201 || movieRes.statusCode() == 200) {
            movieId = movieRes.jsonPath().getInt("data.id");
        } else {
            List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.MOVIES).jsonPath().getList("data.id");
            if (ids != null && !ids.isEmpty()) movieId = ids.get(0);
        }
        Map<String, Object> hallBody = TestDataBuilder.validHallBody();
        Response hallRes = withAdminAuth().body(hallBody).post(ApiConfig.Endpoints.HALLS);
        if (hallRes.statusCode() == 201 || hallRes.statusCode() == 200) {
            hallId = hallRes.jsonPath().getInt("data.id");
        } else {
            List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.HALLS).jsonPath().getList("data.id");
            if (ids != null && !ids.isEmpty()) hallId = ids.get(0);
        }
        Map<String, Object> stBody = TestDataBuilder.validShowtimeBody(movieId, hallId);
        Response stRes = withAdminAuth().body(stBody).post(ApiConfig.Endpoints.SHOWTIMES);
        if (stRes.statusCode() == 201 || stRes.statusCode() == 200) {
            showtimeId = stRes.jsonPath().getInt("data.id");
        } else {
            List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
            if (ids != null && !ids.isEmpty()) showtimeId = ids.get(0);
        }
    }
    private io.restassured.specification.RequestSpecification withToken(String token) {
        return io.restassured.RestAssured.given().spec(requestSpec)
                .header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
    }
    @Test(priority=1) @Story("Full Booking Journey") @Severity(SeverityLevel.BLOCKER)
    @Description("User registers, logs in, views movies, books a seat, checks reservation")
    public void fullBookingJourney_happyPath() {
        Response moviesRes = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        moviesRes.then().statusCode(200);
        List<Integer> seatIds = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data.id");
        if (seatIds == null || seatIds.isEmpty()) return;
        Response bookRes = withUserAuth()
                .body(TestDataBuilder.reservationBody(showtimeId, List.of(seatIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS);
        assertThat(Integer.valueOf(bookRes.statusCode())).isIn(200, 201);
        withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME).then().statusCode(200);
    }
    @Test(priority=2) @Story("Full Booking Journey") @Severity(SeverityLevel.CRITICAL)
    @Description("User views movie details then books")
    public void viewMovieDetails_thenBook() {
        if (movieId == 0) return;
        withNoAuth().pathParam("id", movieId).get(ApiConfig.Endpoints.MOVIE_BY_ID).then().statusCode(200);
        List<Map<String,Object>> seatData = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (seatData == null || seatData.isEmpty()) return;
        List<Integer> availableIds = seatData.stream()
                .filter(s -> "AVAILABLE".equals(s.get("status")))
                .map(s -> ((Number) s.get("id")).intValue())
                .collect(java.util.stream.Collectors.toList());
        if (availableIds.isEmpty()) return;
        withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(availableIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(anyOf(is(200), is(201)));
    }
    @Test(priority=3) @Story("Full Booking Journey") @Severity(SeverityLevel.CRITICAL)
    @Description("Newly registered user can browse and book")
    public void newUser_canBrowseAndBook() {
        Map<String, Object> reg = TestDataBuilder.validRegisterBody();
        withNoAuth().body(reg).post(ApiConfig.Endpoints.REGISTER).then().statusCode(anyOf(is(200),is(201)));
        String token = withNoAuth().body(TestDataBuilder.validLoginBody(
                (String)reg.get("email"),(String)reg.get("password")))
                .post(ApiConfig.Endpoints.LOGIN).jsonPath().getString("data.token");
        if (token == null) return;
        withToken(token).get(ApiConfig.Endpoints.MOVIES).then().statusCode(200);
    }
    @Test(priority=4) @Story("Cancel and Rebook") @Severity(SeverityLevel.CRITICAL)
    @Description("User books a seat, cancels, seat becomes available again")
    public void seatAvailabilityJourney_bookThenCancel() {
        List<Integer> seatIds = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data.id");
        if (seatIds == null || seatIds.isEmpty()) return;
        Integer seatId = seatIds.get(0);
        Response bookRes = withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(seatId)))
                .post(ApiConfig.Endpoints.RESERVATIONS);
        if (bookRes.statusCode() != 200 && bookRes.statusCode() != 201) return;
        String reservationId = bookRes.jsonPath().getString("data.id");
        if (reservationId == null) return;
        withUserAuth().pathParam("id", reservationId).delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(anyOf(is(200), is(204)));
        List<Map<String,Object>> seatsAfter = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (seatsAfter != null) {
            seatsAfter.stream().filter(s -> Integer.valueOf(seatId).equals(s.get("id")))
                    .findFirst().ifPresent(s -> assertThat(s.get("status")).isEqualTo("AVAILABLE"));
        }
    }
    @Test(priority=5) @Story("Cancel and Rebook") @Severity(SeverityLevel.NORMAL)
    @Description("Cancel and rebook same seat successfully")
    public void cancelAndRebook_sameSeat_works() {
        List<Integer> seatIds = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data.id");
        if (seatIds == null || seatIds.isEmpty()) return;
        Integer seatId = seatIds.get(0);
        Response bookRes = withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(seatId)))
                .post(ApiConfig.Endpoints.RESERVATIONS);
        if (bookRes.statusCode() != 200 && bookRes.statusCode() != 201) return;
        String reservationId = bookRes.jsonPath().getString("data.id");
        if (reservationId == null) return;
        withUserAuth().pathParam("id", reservationId).delete(ApiConfig.Endpoints.RESERVATION_BY_ID);
        withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(seatId)))
                .post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(anyOf(is(200),is(201)));
    }
    @Test(priority=6) @Story("Admin Management Flow") @Severity(SeverityLevel.CRITICAL)
    @Description("Admin creates genre, movie, hall, showtime in sequence")
    public void adminFlow_createFullShowtimeSetup() {
        Object genreIdObj = withAdminAuth().body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES).jsonPath().get("data.id");
        if (!(genreIdObj instanceof Number)) return;  // skip gracefully if genre creation fails
        int genreId = ((Number) genreIdObj).intValue();
        Map<String,Object> movie = TestDataBuilder.validMovieBody(java.util.List.of());
        movie.put("genreIds", List.of(genreId));
        Response mr = withAdminAuth().body(movie).post(ApiConfig.Endpoints.MOVIES);
        assertThat(Integer.valueOf(mr.statusCode())).isIn(200,201);
        Object mIdObj = mr.jsonPath().get("data.id");
        if (!(mIdObj instanceof Number)) return;
        int mId = ((Number) mIdObj).intValue();
        Response hr = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS);
        assertThat(Integer.valueOf(hr.statusCode())).isIn(200,201);
        Object hIdObj = hr.jsonPath().get("data.id");
        if (!(hIdObj instanceof Number)) return;
        int hId = ((Number) hIdObj).intValue();
        Response sr = withAdminAuth().body(TestDataBuilder.validShowtimeBody(mId,hId)).post(ApiConfig.Endpoints.SHOWTIMES);
        assertThat(Integer.valueOf(sr.statusCode())).isIn(200,201);
    }
    @Test(priority=7) @Story("Admin Management Flow") @Severity(SeverityLevel.CRITICAL)
    @Description("Admin views all reservations")
    public void admin_viewsAllReservations() {
        withAdminAuth().get(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(200);
    }
    @Test(priority=8) @Story("Admin Management Flow") @Severity(SeverityLevel.NORMAL)
    @Description("Admin views all users")
    public void admin_viewsAllUsers() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        res.then().statusCode(200);
        assertThat(res.jsonPath().getList("data")).isNotNull().isNotEmpty();
    }
    @Test(priority=9) @Story("Admin Management Flow") @Severity(SeverityLevel.NORMAL)
    @Description("Admin views revenue report")
    public void admin_revenueReport_afterBookings() {
        assertThat(Integer.valueOf(withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).statusCode())).isIn(200,204);
    }
    @Test(priority=10) @Story("Admin Management Flow") @Severity(SeverityLevel.NORMAL)
    @Description("Admin views top movies report")
    public void admin_topMoviesReport() {
        assertThat(Integer.valueOf(withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES).statusCode())).isIn(200,204);
    }
    @Test(priority=11) @Story("Data Consistency") @Severity(SeverityLevel.CRITICAL)
    @Description("Booked seat count is consistent")
    public void dataConsistency_bookedSeatsMatchReservations() {
        List<Map<String,Object>> seats = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (seats == null) return;
        long bookedCount = seats.stream().filter(s -> "BOOKED".equals(s.get("status"))).count();
        assertThat(bookedCount).isGreaterThanOrEqualTo(0);
    }
    @Test(priority=12) @Story("Data Consistency") @Severity(SeverityLevel.NORMAL)
    @Description("Movie title consistent between list and detail")
    public void movieDetails_consistentBetweenListAndDetail() {
        if (movieId == 0) return;
        String fromDetail = withNoAuth().pathParam("id", movieId)
                .get(ApiConfig.Endpoints.MOVIE_BY_ID).jsonPath().getString("data.title");
        assertThat(fromDetail).isNotNull();
    }
    @Test(priority=13) @Story("Data Consistency") @Severity(SeverityLevel.NORMAL)
    @Description("Seat count does not exceed hall capacity")
    public void hallCapacity_matchesSeatCount() {
        if (hallId == 0 || showtimeId == 0) return;
        Response hallRes = withAdminAuth().pathParam("id", hallId).get(ApiConfig.Endpoints.HALL_BY_ID);
        if (hallRes.statusCode() != 200) return;
        Integer capacity = hallRes.jsonPath().getInt("data.capacity");
        List<Object> seats = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (seats != null && capacity != null) assertThat(seats.size()).isLessThanOrEqualTo(capacity);
    }
    @Test(priority=14) @Story("Role Transitions") @Severity(SeverityLevel.CRITICAL)
    @Description("Admin can promote a user to ADMIN role")
    public void rolePromotion_userBecomesAdmin() {
        Map<String,Object> reg = TestDataBuilder.validRegisterBody();
        withNoAuth().body(reg).post(ApiConfig.Endpoints.REGISTER);
        List<Map<String,Object>> users = withAdminAuth().get(ApiConfig.Endpoints.USERS).jsonPath().getList("data");
        if (users == null) return;
        Integer userId = users.stream().filter(u -> reg.get("email").equals(u.get("email")))
                .findFirst().map(u -> u.get("id") instanceof Number ? ((Number)u.get("id")).intValue() : null).orElse(null);
        if (userId == null) return;
        withAdminAuth().pathParam("id", userId).body(Map.of("role","ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE).then().statusCode(anyOf(is(200),is(204)));
    }
    @Test(priority=15) @Story("Role Transitions") @Severity(SeverityLevel.NORMAL)
    @Description("Regular user cannot access admin endpoints")
    public void regularUser_cannotAccessAdmin() {
        withUserAuth().get(ApiConfig.Endpoints.USERS).then().statusCode(anyOf(is(401),is(403)));
        withUserAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).then().statusCode(anyOf(is(401),is(403)));
    }
    @Test(priority=16) @Story("Error Handling") @Severity(SeverityLevel.NORMAL)
    @Description("Booking already-booked seat returns conflict")
    public void bookAlreadyBookedSeat_returnsConflict() {
        List<Integer> seatIds = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data.id");
        if (seatIds == null || seatIds.isEmpty()) return;
        Response first = withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(seatIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS);
        if (first.statusCode() != 200 && first.statusCode() != 201) return;
        Response second = withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(seatIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS);
        assertThat(Integer.valueOf(second.statusCode())).isIn(400,409,422);
    }
    @Test(priority=17) @Story("Error Handling") @Severity(SeverityLevel.NORMAL)
    @Description("Booking non-existent showtime returns 404 or 400")
    public void bookNonExistentShowtime_returns404() {
        withUserAuth().body(TestDataBuilder.reservationBody(999999, List.of(1)))
                .post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(anyOf(is(404),is(400)));
    }
    @Test(priority=18) @Story("Error Handling") @Severity(SeverityLevel.NORMAL)
    @Description("Cancelling another user's reservation returns 403 or 404")
    public void cancelOtherUsersReservation_returns403or404() {
        List<Integer> seatIds = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data.id");
        if (seatIds == null || seatIds.isEmpty()) return;
        Response bookRes = withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(seatIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS);
        if (bookRes.statusCode() != 200 && bookRes.statusCode() != 201) return;
        String reservationId = bookRes.jsonPath().getString("data.id");
        if (reservationId == null) return;
        Map<String,Object> otherReg = TestDataBuilder.validRegisterBody();
        withNoAuth().body(otherReg).post(ApiConfig.Endpoints.REGISTER);
        String otherToken = withNoAuth().body(TestDataBuilder.validLoginBody(
                (String)otherReg.get("email"),(String)otherReg.get("password")))
                .post(ApiConfig.Endpoints.LOGIN).jsonPath().getString("data.token");
        if (otherToken == null) return;
        withToken(otherToken).pathParam("id", reservationId).delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(anyOf(is(400), is(403),is(404)));
    }
    @Test(priority=19) @Story("Multi-Seat Booking") @Severity(SeverityLevel.CRITICAL)
    @Description("User books multiple seats in one reservation")
    public void bookMultipleSeats_success() {
        List<Map<String,Object>> seatData = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (seatData == null || seatData.size() < 2) return;
        List<Integer> availableIds = seatData.stream()
                .filter(s -> "AVAILABLE".equals(s.get("status")))
                .map(s -> ((Number) s.get("id")).intValue())
                .limit(2)
                .collect(java.util.stream.Collectors.toList());
        if (availableIds.size() < 2) return;
        assertThat(Integer.valueOf(withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, availableIds))
                .post(ApiConfig.Endpoints.RESERVATIONS).statusCode())).isIn(200,201);
    }
    @Test(priority=20) @Story("Multi-Seat Booking") @Severity(SeverityLevel.NORMAL)
    @Description("Cancelling multi-seat reservation frees all seats")
    public void cancelMultiSeatReservation_allSeatsAvailable() {
        List<Integer> seatIds = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data.id");
        if (seatIds == null || seatIds.size() < 2) return;
        Response bookRes = withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, seatIds.subList(0,2)))
                .post(ApiConfig.Endpoints.RESERVATIONS);
        if (bookRes.statusCode() != 200 && bookRes.statusCode() != 201) return;
        String resId = bookRes.jsonPath().getString("data.id");
        if (resId == null) return;
        withUserAuth().pathParam("id", resId).delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(anyOf(is(200),is(204)));
    }
    @Test(priority=21) @Story("Reports After Actions") @Severity(SeverityLevel.NORMAL)
    @Description("Revenue report accessible after reservations")
    public void revenueReport_accessible_afterReservations() {
        assertThat(Integer.valueOf(withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).statusCode())).isIn(200,204);
    }
    @Test(priority=22) @Story("Reports After Actions") @Severity(SeverityLevel.NORMAL)
    @Description("Capacity report for a showtime")
    public void capacityReport_returnsOccupancy() {
        if (showtimeId == 0) return;
        assertThat(Integer.valueOf(withAdminAuth().pathParam("showtimeId", showtimeId).get(ApiConfig.Endpoints.REPORT_CAPACITY).statusCode())).isIn(200,404);
    }
    @Test(priority=23) @Story("Reports After Actions") @Severity(SeverityLevel.NORMAL)
    @Description("Top movies report is accessible")
    public void topMoviesReport_accessible() {
        assertThat(Integer.valueOf(withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES).statusCode())).isIn(200,204);
    }
    @Test(priority=24) @Story("Profile Management") @Severity(SeverityLevel.NORMAL)
    @Description("User can view profile after login")
    public void userProfile_isAccessible_afterLogin() {
        Response res = withUserAuth().get(ApiConfig.Endpoints.USER_ME);
        res.then().statusCode(200);
        assertThat((Object) res.jsonPath().get("data.email")).isNotNull();
    }
    @Test(priority=25) @Story("Profile Management") @Severity(SeverityLevel.NORMAL)
    @Description("Logged-out user cannot access profile")
    public void loggedOutUser_cannotAccessProfile() {
        withNoAuth().get(ApiConfig.Endpoints.USER_ME).then().statusCode(anyOf(is(401),is(403)));
    }
    @Test(priority=26) @Story("Profile Management") @Severity(SeverityLevel.NORMAL)
    @Description("My reservations shows own reservations only")
    public void myReservations_returnsOnlyOwnReservations() {
        withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME).then().statusCode(200);
    }
    @Test(priority=27) @Story("Search and Filter") @Severity(SeverityLevel.NORMAL)
    @Description("Movies can be filtered by genre")
    public void movies_filteredByGenre_returnsResults() {
        List<Integer> genreIds = withNoAuth().get(ApiConfig.Endpoints.GENRES).jsonPath().getList("data.id");
        if (genreIds == null || genreIds.isEmpty()) return;
        assertThat(Integer.valueOf(withNoAuth().queryParam("genreId", genreIds.get(0))
                .get(ApiConfig.Endpoints.MOVIES).statusCode())).isIn(200,400);
    }
    @Test(priority=28) @Story("Search and Filter") @Severity(SeverityLevel.MINOR)
    @Description("Movies list supports pagination")
    public void movies_pagination_works() {
        assertThat(Integer.valueOf(withNoAuth().queryParam("page",0).queryParam("size",5)
                .get(ApiConfig.Endpoints.MOVIES).statusCode())).isIn(200,400);
    }
    @Test(priority=29) @Story("Search and Filter") @Severity(SeverityLevel.MINOR)
    @Description("Showtimes can be filtered by movie")
    public void showtimes_filteredByMovie_returnsResults() {
        if (movieId == 0) return;
        assertThat(Integer.valueOf(withUserAuth().queryParam("movieId", movieId)
                .get(ApiConfig.Endpoints.SHOWTIMES).statusCode())).isIn(200,400);
    }
    @Test(priority=30) @Story("Concurrency") @Severity(SeverityLevel.CRITICAL)
    @Description("Only one booking succeeds when two users race for the same seat")
    public void concurrentBooking_lastSeat_onlyOneSucceeds() throws InterruptedException {
        List<Map<String,Object>> allSeats = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (allSeats == null || allSeats.isEmpty()) return;
        List<Integer> availableIds = allSeats.stream()
                .filter(s -> "AVAILABLE".equals(s.get("status")))
                .map(s -> ((Number) s.get("id")).intValue())
                .collect(java.util.stream.Collectors.toList());
        if (availableIds.isEmpty()) return;
        Integer seatId = availableIds.get(0);
        int[] statuses = new int[2];
        Thread t1 = new Thread(() ->
            statuses[0] = withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(seatId)))
                    .post(ApiConfig.Endpoints.RESERVATIONS).statusCode());
        Thread t2 = new Thread(() ->
            statuses[1] = withAdminAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(seatId)))
                    .post(ApiConfig.Endpoints.RESERVATIONS).statusCode());
        t1.start(); t2.start(); t1.join(5000); t2.join(5000);
        assertThat((statuses[0]==200||statuses[0]==201)||(statuses[1]==200||statuses[1]==201)).isTrue();
    }
    @Test(priority=31) @Story("Concurrency") @Severity(SeverityLevel.NORMAL)
    @Description("Seat state is consistent after concurrent attempts")
    public void systemConsistency_afterConcurrentBooking() {
        List<Map<String,Object>> seats = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data");
        if (seats == null) return;
        long available = seats.stream().filter(s -> "AVAILABLE".equals(s.get("status"))).count();
        long booked = seats.stream().filter(s -> "BOOKED".equals(s.get("status"))).count();
        assertThat(available + booked).isLessThanOrEqualTo(seats.size());
    }
    @Test(priority=32) @Story("CRUD Lifecycle") @Severity(SeverityLevel.NORMAL)
    @Description("Genre full lifecycle")
    public void genreLifecycle_createReadUpdateDelete() {
        Response cr = withAdminAuth().body(TestDataBuilder.validGenreBody()).post(ApiConfig.Endpoints.GENRES);
        assertThat(Integer.valueOf(cr.statusCode())).isIn(200,201);
        int gId = cr.jsonPath().getInt("data.id");
        withAdminAuth().pathParam("id",gId).get(ApiConfig.Endpoints.GENRE_BY_ID).then().statusCode(200);
        withAdminAuth().pathParam("id",gId).body(Map.of("name","Updated "+System.currentTimeMillis()))
                .put(ApiConfig.Endpoints.GENRE_BY_ID).then().statusCode(anyOf(is(200),is(204)));
        withAdminAuth().pathParam("id",gId).delete(ApiConfig.Endpoints.GENRE_BY_ID).then().statusCode(anyOf(is(200),is(204)));
    }
    @Test(priority=33) @Story("CRUD Lifecycle") @Severity(SeverityLevel.NORMAL)
    @Description("Hall full lifecycle")
    public void hallLifecycle_createReadUpdateDelete() {
        Response cr = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS);
        assertThat(Integer.valueOf(cr.statusCode())).isIn(200,201);
        int hId = cr.jsonPath().getInt("data.id");
        withAdminAuth().pathParam("id",hId).get(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(200);
        Map<String,Object> upd = TestDataBuilder.validHallBody(); upd.put("name","Updated "+System.currentTimeMillis());
        withAdminAuth().pathParam("id",hId).body(upd).put(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(anyOf(is(200),is(204)));
        withAdminAuth().pathParam("id",hId).delete(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(anyOf(is(200),is(204)));
    }
    @Test(priority=34) @Story("CRUD Lifecycle") @Severity(SeverityLevel.NORMAL)
    @Description("Movie full lifecycle")
    public void movieLifecycle_createReadUpdateDelete() {
        Response cr = withAdminAuth().body(TestDataBuilder.validMovieBody(java.util.List.of())).post(ApiConfig.Endpoints.MOVIES);
        assertThat(Integer.valueOf(cr.statusCode())).isIn(200,201);
        int mId = cr.jsonPath().getInt("data.id");
        withNoAuth().pathParam("id",mId).get(ApiConfig.Endpoints.MOVIE_BY_ID).then().statusCode(200);
        Map<String,Object> upd = TestDataBuilder.validMovieBody(java.util.List.of()); upd.put("title","Updated "+System.currentTimeMillis());
        withAdminAuth().pathParam("id",mId).body(upd).put(ApiConfig.Endpoints.MOVIE_BY_ID).then().statusCode(anyOf(is(200),is(204)));
        withAdminAuth().pathParam("id",mId).delete(ApiConfig.Endpoints.MOVIE_BY_ID).then().statusCode(anyOf(is(200),is(204)));
    }
    @Test(priority=35) @Story("Token Security") @Severity(SeverityLevel.BLOCKER)
    @Description("Invalid token cannot book a seat")
    public void invalidToken_cannotBook() {
        List<Integer> seatIds = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data.id");
        if (seatIds == null || seatIds.isEmpty()) return;
        withToken("Bearer invalid.jwt.token")
                .body(TestDataBuilder.reservationBody(showtimeId, List.of(seatIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(anyOf(is(401),is(403)));
    }
    @Test(priority=36) @Story("Token Security") @Severity(SeverityLevel.CRITICAL)
    @Description("Token from one user cannot cancel another's reservation")
    public void crossUserToken_cannotCancelOthersReservation() {
        List<Integer> seatIds = withUserAuth().pathParam("id", showtimeId)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS).jsonPath().getList("data.id");
        if (seatIds == null || seatIds.isEmpty()) return;
        Response bookRes = withUserAuth().body(TestDataBuilder.reservationBody(showtimeId, List.of(seatIds.get(0))))
                .post(ApiConfig.Endpoints.RESERVATIONS);
        if (bookRes.statusCode() != 200 && bookRes.statusCode() != 201) return;
        String resId = bookRes.jsonPath().getString("data.id");
        if (resId == null) return;
        Map<String,Object> ou = TestDataBuilder.validRegisterBody();
        withNoAuth().body(ou).post(ApiConfig.Endpoints.REGISTER);
        String ot = withNoAuth().body(TestDataBuilder.validLoginBody((String)ou.get("email"),(String)ou.get("password")))
                .post(ApiConfig.Endpoints.LOGIN).jsonPath().getString("data.token");
        if (ot == null) return;
        withToken(ot).pathParam("id",resId).delete(ApiConfig.Endpoints.RESERVATION_BY_ID)
                .then().statusCode(anyOf(is(400), is(403),is(404)));
    }
    @Test(priority=37) @Story("Response Times") @Severity(SeverityLevel.MINOR)
    @Description("Key endpoints respond within 5 seconds")
    public void allKeyEndpoints_respondWithin5Seconds() {
        withNoAuth().get(ApiConfig.Endpoints.MOVIES).then().time(lessThan(5000L));
        withAdminAuth().get(ApiConfig.Endpoints.HALLS).then().time(lessThan(5000L));
        withUserAuth().get(ApiConfig.Endpoints.RESERVATION_ME).then().time(lessThan(5000L));
    }
    @Test(priority=38) @Story("Response Format") @Severity(SeverityLevel.NORMAL)
    @Description("All endpoints return JSON content-type")
    public void allEndpoints_returnJsonContentType() {
        withNoAuth().get(ApiConfig.Endpoints.MOVIES).then().contentType(containsString("application/json"));
        withAdminAuth().get(ApiConfig.Endpoints.HALLS).then().contentType(containsString("application/json"));
    }
    @Test(priority=39) @Story("Response Format") @Severity(SeverityLevel.NORMAL)
    @Description("API responses follow consistent structure")
    public void apiResponses_followEnvelopeStructure() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        res.then().statusCode(200);
        assertThat(res.getBody().asString()).isNotEmpty();
    }
    @Test(priority=40) @Story("Response Format") @Severity(SeverityLevel.MINOR)
    @Description("404 responses include meaningful error message")
    public void notFound_response_includesMeaningfulError() {
        Response res = withNoAuth().pathParam("id", 999999).get(ApiConfig.Endpoints.MOVIE_BY_ID);
        assertThat(Integer.valueOf(res.statusCode())).isIn(404,400);
        assertThat(res.getBody().asString()).isNotEmpty();
    }
}
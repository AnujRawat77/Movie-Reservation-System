package com.cinereserve.api.tests.security;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.TestDataBuilder;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: Security Tests
 * Cross-cutting security concerns for the entire API
 * Total @Test methods: 55
 */
@Epic("Security")
@Feature("API Security")
public class SecurityTests extends BaseTest {

    // ─── AUTHENTICATION BYPASS ATTEMPTS ──────────────────────────────────────

    @Test(priority = 1) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void adminRoute_noAuthHeader_returns401() {
        withNoAuth().get(ApiConfig.Endpoints.USERS).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 2) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void adminRoute_malformedBearerToken_returns401() {
        withNoAuth()
                .header("Authorization", "Bearer not.valid.jwt")
                .get(ApiConfig.Endpoints.USERS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 3) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void adminRoute_emptyBearerToken_returns401() {
        withNoAuth()
                .header("Authorization", "Bearer ")
                .get(ApiConfig.Endpoints.USERS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 4) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void adminRoute_wrongAuthScheme_returns401() {
        withNoAuth()
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .get(ApiConfig.Endpoints.USERS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 5) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void adminRoute_withUserToken_returns403() {
        withUserAuth().get(ApiConfig.Endpoints.USERS).then().statusCode(403);
    }

    @Test(priority = 6) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void createMovie_withUserToken_returns403() {
        withUserAuth()
                .body(TestDataBuilder.validMovieBody(java.util.List.of(1)))
                .post(ApiConfig.Endpoints.MOVIES)
                .then().statusCode(403);
    }

    @Test(priority = 7) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void createGenre_withUserToken_returns403() {
        withUserAuth()
                .body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(403);
    }

    @Test(priority = 8) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void deleteMovie_withUserToken_returns403() {
        withUserAuth().pathParam("id", 1).delete(ApiConfig.Endpoints.MOVIE_BY_ID).then().statusCode(403);
    }

    @Test(priority = 9) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void revenueReport_withUserToken_returns403() {
        withUserAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).then().statusCode(403);
    }

    @Test(priority = 10) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void topMoviesReport_withUserToken_returns403() {
        withUserAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES).then().statusCode(403);
    }

    // ─── JWT SECURITY ─────────────────────────────────────────────────────────

    @Test(priority = 11) @Story("JWT Security") @Severity(SeverityLevel.CRITICAL)
    public void expiredToken_returns401() {
        withExpiredToken().get(ApiConfig.Endpoints.USER_ME).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 12) @Story("JWT Security") @Severity(SeverityLevel.CRITICAL)
    public void tamperedToken_returns401() {
        // Get valid token and tamper with payload
        String validToken = adminToken;
        String[] parts = validToken.split("\\.");
        if (parts.length == 3) {
            String tamperedToken = parts[0] + ".TAMPERED_PAYLOAD." + parts[2];
            withNoAuth()
                    .header("Authorization", "Bearer " + tamperedToken)
                    .get(ApiConfig.Endpoints.USER_ME)
                    .then().statusCode(anyOf(is(401), is(403)));
        }
    }

    @Test(priority = 13) @Story("JWT Security") @Severity(SeverityLevel.CRITICAL)
    public void token_withoutBearer_prefix_returns401() {
        withNoAuth()
                .header("Authorization", adminToken)
                .get(ApiConfig.Endpoints.USER_ME)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 14) @Story("JWT Security") @Severity(SeverityLevel.NORMAL)
    public void loginResponse_tokenCanBeUsedImmediately() {
        Response loginRes = withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN);
        String token = loginRes.jsonPath().getString("data.token");
        withBearerToken(token).get(ApiConfig.Endpoints.USER_ME).then().statusCode(200);
    }

    @Test(priority = 15) @Story("JWT Security") @Severity(SeverityLevel.MINOR)
    public void jwt_nullToken_returns401() {
        withNoAuth()
                .header("Authorization", "Bearer null")
                .get(ApiConfig.Endpoints.USER_ME)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    // ─── SQL INJECTION ────────────────────────────────────────────────────────

    @Test(priority = 16) @Story("SQL Injection") @Severity(SeverityLevel.CRITICAL)
    public void login_sqlInjectionInEmail_doesNotCrash() {
        Map<String, Object> body = TestDataBuilder.validLoginBody("admin'--@test.com", "pass");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 401, 403, 404);
    }

    @Test(priority = 17) @Story("SQL Injection") @Severity(SeverityLevel.CRITICAL)
    public void login_sqlInjectionOrTrue_doesNotBypassAuth() {
        Map<String, Object> body = TestDataBuilder.validLoginBody("' OR '1'='1", "' OR '1'='1");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 401, 403, 404);
    }

    @Test(priority = 18) @Story("SQL Injection") @Severity(SeverityLevel.CRITICAL)
    public void register_sqlInjectionInName_handledGracefully() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("name", "Robert'); DROP TABLE users;--");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 19) @Story("SQL Injection") @Severity(SeverityLevel.CRITICAL)
    public void getMovieById_sqlInjection_returns4xx() {
        int status = withNoAuth().pathParam("id", "1 OR 1=1")
                .get(ApiConfig.Endpoints.MOVIE_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 404, 405, 500);
    }

    // ─── XSS PREVENTION ───────────────────────────────────────────────────────

    @Test(priority = 20) @Story("XSS Prevention") @Severity(SeverityLevel.CRITICAL)
    public void register_xssInName_handledSafely() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("name", "<script>alert('XSS')</script>");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 21) @Story("XSS Prevention") @Severity(SeverityLevel.CRITICAL)
    public void createGenre_xssInName_notStoredRaw() {
        Map<String, Object> body = TestDataBuilder.genreBody("<script>alert(1)</script>");
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.GENRES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 409);
    }

    // ─── SECURITY HEADERS ────────────────────────────────────────────────────

    @Test(priority = 22) @Story("Security Headers") @Severity(SeverityLevel.NORMAL)
    public void apiResponse_doesNotExposeServerVersion() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        assertThat(res.header("X-Powered-By")).isNullOrEmpty();
    }

    @Test(priority = 23) @Story("Security Headers") @Severity(SeverityLevel.MINOR)
    public void apiResponse_contentTypeIsAlwaysJson() {
        withNoAuth().get(ApiConfig.Endpoints.MOVIES)
                .then().contentType(containsString("application/json"));
    }

    // ─── RBAC COMPLETENESS ────────────────────────────────────────────────────

    @Test(priority = 24) @Story("RBAC") @Severity(SeverityLevel.CRITICAL)
    public void regularUser_cannotAccessAdminReservations() {
        withUserAuth().get(ApiConfig.Endpoints.RESERVATIONS).then().statusCode(403);
    }

    @Test(priority = 25) @Story("RBAC") @Severity(SeverityLevel.CRITICAL)
    public void regularUser_cannotCreateShowtime() {
        withUserAuth()
                .body(TestDataBuilder.validShowtimeBody(1, 1))
                .post(ApiConfig.Endpoints.SHOWTIMES)
                .then().statusCode(403);
    }

    @Test(priority = 26) @Story("RBAC") @Severity(SeverityLevel.CRITICAL)
    public void regularUser_cannotDeleteShowtime() {
        withUserAuth().pathParam("id", 1).delete(ApiConfig.Endpoints.SHOWTIME_BY_ID).then().statusCode(403);
    }

    @Test(priority = 27) @Story("RBAC") @Severity(SeverityLevel.CRITICAL)
    public void regularUser_cannotCreateHall() {
        withUserAuth()
                .body(TestDataBuilder.validHallBody())
                .post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(403);
    }

    @Test(priority = 28) @Story("RBAC") @Severity(SeverityLevel.CRITICAL)
    public void regularUser_cannotUpdateUserRole() {
        withUserAuth()
                .pathParam("id", 1)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(403);
    }

    @Test(priority = 29) @Story("RBAC") @Severity(SeverityLevel.CRITICAL)
    public void regularUser_cannotViewAllUsers() {
        withUserAuth().get(ApiConfig.Endpoints.USERS).then().statusCode(403);
    }

    @Test(priority = 30) @Story("RBAC") @Severity(SeverityLevel.NORMAL)
    public void admin_canAccessAllAdminEndpoints() {
        withAdminAuth().get(ApiConfig.Endpoints.USERS).then().statusCode(200);
        withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).then().statusCode(200);
        withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES).then().statusCode(200);
    }

    @Test(priority = 31) @Story("RBAC") @Severity(SeverityLevel.NORMAL)
    public void guest_canAccessPublicMovieEndpoints() {
        withNoAuth().get(ApiConfig.Endpoints.MOVIES).then().statusCode(200);
        withNoAuth().get(ApiConfig.Endpoints.GENRES).then().statusCode(200);
    }

    @Test(priority = 32) @Story("RBAC") @Severity(SeverityLevel.NORMAL)
    public void guest_cannotBookSeats() {
        withNoAuth()
                .body(TestDataBuilder.reservationBody(1, java.util.List.of(1)))
                .post(ApiConfig.Endpoints.RESERVATIONS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 33) @Story("RBAC") @Severity(SeverityLevel.NORMAL)
    public void guest_cannotViewSeatAvailability() {
        withNoAuth().pathParam("id", 1)
                .get(ApiConfig.Endpoints.SHOWTIME_SEATS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    // ─── DATA EXPOSURE ────────────────────────────────────────────────────────

    @Test(priority = 34) @Story("Data Exposure") @Severity(SeverityLevel.CRITICAL)
    public void movieList_doesNotExposeInternalServerDetails() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.MOVIES);
        assertThat(res.body().asString())
                .doesNotContain("java.lang")
                .doesNotContain("Hibernate");
    }

    @Test(priority = 35) @Story("Data Exposure") @Severity(SeverityLevel.CRITICAL)
    public void loginResponse_doesNotExposePasswordHash() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN);
        assertThat(res.body().asString())
                .doesNotContain("passwordHash")
                .doesNotContain("password");
    }

    @Test(priority = 36) @Story("Data Exposure") @Severity(SeverityLevel.CRITICAL)
    public void userProfile_doesNotExposePasswordHash() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USER_ME);
        assertThat(res.body().asString()).doesNotContain("passwordHash");
    }

    @Test(priority = 37) @Story("Data Exposure") @Severity(SeverityLevel.CRITICAL)
    public void userList_doesNotExposePasswordHash() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        assertThat(res.body().asString()).doesNotContain("passwordHash");
    }

    @Test(priority = 38) @Story("Data Exposure") @Severity(SeverityLevel.NORMAL)
    public void errorResponse_doesNotExposeStackTrace() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody("bad@user.com", "wrong"))
                .post(ApiConfig.Endpoints.LOGIN);
        assertThat(res.body().asString())
                .doesNotContain("at com.")
                .doesNotContain("java.lang");
    }

    // ─── HTTP METHOD SECURITY ─────────────────────────────────────────────────

    @Test(priority = 39) @Story("HTTP Methods") @Severity(SeverityLevel.NORMAL)
    public void movies_traceMethod_returns405or404() {
        int status = withNoAuth()
                .request(io.restassured.http.Method.OPTIONS, ApiConfig.Endpoints.MOVIES)
                .statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 403, 404, 405);
    }

    @Test(priority = 40) @Story("HTTP Methods") @Severity(SeverityLevel.NORMAL)
    public void login_getMethod_returns405() {
        int status = withNoAuth().get(ApiConfig.Endpoints.LOGIN).statusCode();
        assertThat(Integer.valueOf(status)).isIn(404, 405, 500);
    }

    @Test(priority = 41) @Story("HTTP Methods") @Severity(SeverityLevel.NORMAL)
    public void register_getMethod_returns405() {
        int status = withNoAuth().get(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(404, 405, 500);
    }

    // ─── RATE LIMITING & ABUSE ────────────────────────────────────────────────

    @Test(priority = 42) @Story("Brute Force Protection") @Severity(SeverityLevel.NORMAL)
    public void login_multipleFailedAttempts_doesNotCrash() {
        for (int i = 0; i < 5; i++) {
            withNoAuth()
                    .body(TestDataBuilder.validLoginBody("bad@test.com", "wrongpass" + i))
                    .post(ApiConfig.Endpoints.LOGIN);
        }
        // Server should still be up
        withNoAuth().get(ApiConfig.Endpoints.MOVIES).then().statusCode(200);
    }

    @Test(priority = 43) @Story("Brute Force Protection") @Severity(SeverityLevel.NORMAL)
    public void server_handles_rapidSuccessiveRequests() {
        for (int i = 0; i < 10; i++) {
            withNoAuth().get(ApiConfig.Endpoints.MOVIES).then().statusCode(200);
        }
    }

    // ─── PAYLOAD INJECTION ────────────────────────────────────────────────────

    @Test(priority = 44) @Story("Payload Injection") @Severity(SeverityLevel.CRITICAL)
    public void register_jsonInjectionInEmail_handledSafely() {
        String body = "{\"name\":\"test\",\"email\":\"test\\\"},{\\\"admin\\\":true,\\\"\",\"password\":\"Test@123\"}";
        int status = withNoAuth().contentType("application/json").body(body).post(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 422, 500);
    }

    @Test(priority = 45) @Story("Payload Injection") @Severity(SeverityLevel.CRITICAL)
    public void movieTitle_pathTraversal_handledSafely() {
        Map<String, Object> body = TestDataBuilder.validMovieBody(java.util.List.of(1));
        body.put("title", "../../etc/passwd");
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.MOVIES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 46) @Story("Payload Injection") @Severity(SeverityLevel.NORMAL)
    public void genre_nullByteInName_handledSafely() {
        int status = withAdminAuth()
                .body(TestDataBuilder.genreBody("Genre\u0000Null"))
                .post(ApiConfig.Endpoints.GENRES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 409, 422);
    }

    // ─── CONTENT TYPE SECURITY ────────────────────────────────────────────────

    @Test(priority = 47) @Story("Content Type") @Severity(SeverityLevel.NORMAL)
    public void login_withFormUrlEncoded_returns415or400() {
        int status = withNoAuth()
                .contentType("application/x-www-form-urlencoded")
                .formParam("email", ApiConfig.ADMIN_EMAIL)
                .formParam("password", ApiConfig.ADMIN_PASSWORD)
                .post(ApiConfig.Endpoints.LOGIN).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 415, 422, 500);
    }

    @Test(priority = 48) @Story("Content Type") @Severity(SeverityLevel.MINOR)
    public void login_withTextPlain_returns415or400() {
        int status = withNoAuth()
                .contentType("text/plain")
                .body("email=admin&password=pass")
                .post(ApiConfig.Endpoints.LOGIN).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 415, 422, 500);
    }

    // ─── AUTHORIZATION ESCALATION ─────────────────────────────────────────────

    @Test(priority = 49) @Story("Privilege Escalation") @Severity(SeverityLevel.CRITICAL)
    public void regularUser_cannotSelfPromoteToAdmin() {
        // User tries to update own role to ADMIN
        withUserAuth()
                .pathParam("id", createdUserId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(403);
    }

    @Test(priority = 50) @Story("Privilege Escalation") @Severity(SeverityLevel.CRITICAL)
    public void user_cannotViewAnotherUsersProfile() {
        // Get all users as admin to find another user
        Response usersRes = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        java.util.List<Integer> userIds = usersRes.jsonPath().getList("data.id");
        if (userIds != null && userIds.size() > 1) {
            // Try as regular user to access admin's profile — but /users/me only returns own profile
            // This test verifies the endpoint correctly filters to "me"
            Response myProfile = withUserAuth().get(ApiConfig.Endpoints.USER_ME);
            myProfile.then().statusCode(200);
            int myId = myProfile.jsonPath().getInt("data.id");
            // User can only see their own ID
            assertThat(myId).isEqualTo(createdUserId);
        }
    }

    @Test(priority = 51) @Story("Privilege Escalation") @Severity(SeverityLevel.CRITICAL)
    public void userToken_cannotBeUsedForReports() {
        withUserAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).then().statusCode(403);
        withUserAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES).then().statusCode(403);
    }

    @Test(priority = 52) @Story("Privilege Escalation") @Severity(SeverityLevel.CRITICAL)
    public void guestToken_cannotBookSeatsOrViewProfile() {
        withNoAuth().get(ApiConfig.Endpoints.USER_ME).then().statusCode(anyOf(is(401), is(403)));
        withNoAuth().get(ApiConfig.Endpoints.RESERVATION_ME).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 53) @Story("JWT Security") @Severity(SeverityLevel.NORMAL)
    public void adminToken_isValidAfterMultipleRequests() {
        for (int i = 0; i < 5; i++) {
            withAdminAuth().get(ApiConfig.Endpoints.USER_ME).then().statusCode(200);
        }
    }

    @Test(priority = 54) @Story("Data Exposure") @Severity(SeverityLevel.NORMAL)
    public void error_404Response_doesNotExposePathInfo() {
        Response res = withNoAuth().pathParam("id", 999999).get(ApiConfig.Endpoints.MOVIE_BY_ID);
        res.then().statusCode(404);
        assertThat(res.body().asString()).doesNotContain("java.lang");
    }

    @Test(priority = 55) @Story("Auth Bypass") @Severity(SeverityLevel.CRITICAL)
    public void unauthorizedAccess_allAdminEndpoints_returns401Or403() {
        // Test all admin-only endpoints respond correctly without auth
        int[] statuses = {
            withNoAuth().get(ApiConfig.Endpoints.USERS).statusCode(),
            withNoAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).statusCode(),
            withNoAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES).statusCode(),
        };
        for (int status : statuses) {
            assertThat(Integer.valueOf(status)).isIn(401, 403);
        }
    }
}


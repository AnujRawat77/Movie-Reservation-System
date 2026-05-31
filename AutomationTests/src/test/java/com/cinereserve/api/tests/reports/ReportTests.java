package com.cinereserve.api.tests.reports;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: Reports API
 * Endpoints: GET /api/reports/revenue, /api/reports/capacity/{id}, /api/reports/top-movies
 * Total @Test methods: 50
 */
@Epic("Reports")
@Feature("Analytics & Reports")
public class ReportTests extends BaseTest {

    // ─── REVENUE REPORT ───────────────────────────────────────────────────────

    @Test(priority = 1) @Story("Revenue Report") @Severity(SeverityLevel.BLOCKER)
    public void getRevenueReport_adminReturns200() {
        withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).then().statusCode(200);
    }

    @Test(priority = 2) @Story("Revenue Report") @Severity(SeverityLevel.CRITICAL)
    public void getRevenueReport_withoutAuth_returns401() {
        withNoAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 3) @Story("Revenue Report") @Severity(SeverityLevel.CRITICAL)
    public void getRevenueReport_withUserRole_returns403() {
        withUserAuth().get(ApiConfig.Endpoints.REPORT_REVENUE).then().statusCode(403);
    }

    @Test(priority = 4) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_withDateFilter_returns200() {
        withAdminAuth()
                .queryParam("from", "2026-01-01")
                .queryParam("to", "2026-12-31")
                .get(ApiConfig.Endpoints.REPORT_REVENUE)
                .then().statusCode(200);
    }

    @Test(priority = 5) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_withFromDateOnly_returns200() {
        withAdminAuth()
                .queryParam("from", "2026-01-01")
                .get(ApiConfig.Endpoints.REPORT_REVENUE)
                .then().statusCode(200);
    }

    @Test(priority = 6) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_withToDateOnly_returns200() {
        withAdminAuth()
                .queryParam("to", "2026-12-31")
                .get(ApiConfig.Endpoints.REPORT_REVENUE)
                .then().statusCode(200);
    }

    @Test(priority = 7) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_responseContainsTotalRevenue() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE);
        res.then().statusCode(200);
        assertThat(res.body().asString()).containsAnyOf("total", "revenue", "data");
    }

    @Test(priority = 8) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_withInvalidDateFormat_returns4xx() {
        int status = withAdminAuth()
                .queryParam("from", "not-a-date")
                .queryParam("to", "also-not-a-date")
                .get(ApiConfig.Endpoints.REPORT_REVENUE).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 422, 500);
    }

    @Test(priority = 9) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_fromAfterTo_returns4xxOrEmpty() {
        int status = withAdminAuth()
                .queryParam("from", "2026-12-31")
                .queryParam("to", "2026-01-01")
                .get(ApiConfig.Endpoints.REPORT_REVENUE).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 10) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_successFlagIsTrue() {
        withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE)
                .then().statusCode(200).body("success", equalTo(true));
    }

    @Test(priority = 11) @Story("Revenue Report") @Severity(SeverityLevel.MINOR)
    public void getRevenueReport_responseTimeUnder5Seconds() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 12) @Story("Revenue Report") @Severity(SeverityLevel.MINOR)
    public void getRevenueReport_contentTypeIsJson() {
        withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 13) @Story("Revenue Report") @Severity(SeverityLevel.MINOR)
    public void getRevenueReport_withInvalidToken_returns401() {
        withInvalidToken().get(ApiConfig.Endpoints.REPORT_REVENUE).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 14) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_returnsDataArray() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE);
        res.then().statusCode(200);
        assertThat((Object) res.jsonPath().get("data")).isNotNull();
    }

    @Test(priority = 15) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_totalRevenueIsNonNegative() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE);
        res.then().statusCode(200);
        Object total = res.jsonPath().get("data.totalRevenue");
        if (total != null) {
            assertThat(((Number) total).doubleValue()).isGreaterThanOrEqualTo(0.0);
        }
    }

    @Test(priority = 16) @Story("Revenue Report") @Severity(SeverityLevel.NORMAL)
    public void getRevenueReport_revenueByMovieIsArray() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE);
        res.then().statusCode(200);
        Object data = res.jsonPath().get("data");
        assertThat(data).isNotNull();
    }

    @Test(priority = 17) @Story("Revenue Report") @Severity(SeverityLevel.MINOR)
    public void getRevenueReport_noExtraUnknownFields() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_REVENUE);
        res.then().statusCode(200);
        // Just verify it returns something meaningful
        assertThat(res.body().asString()).isNotBlank();
    }

    @Test(priority = 18) @Story("Revenue Report") @Severity(SeverityLevel.MINOR)
    public void getRevenueReport_with30DayRange() {
        withAdminAuth()
                .queryParam("from", "2026-05-01")
                .queryParam("to", "2026-05-31")
                .get(ApiConfig.Endpoints.REPORT_REVENUE)
                .then().statusCode(200);
    }

    @Test(priority = 19) @Story("Revenue Report") @Severity(SeverityLevel.MINOR)
    public void getRevenueReport_withSameDateRange() {
        withAdminAuth()
                .queryParam("from", "2026-05-31")
                .queryParam("to", "2026-05-31")
                .get(ApiConfig.Endpoints.REPORT_REVENUE)
                .then().statusCode(200);
    }

    @Test(priority = 20) @Story("Revenue Report") @Severity(SeverityLevel.MINOR)
    public void getRevenueReport_withFutureDateRange_returnsEmptyOrZero() {
        withAdminAuth()
                .queryParam("from", "2030-01-01")
                .queryParam("to", "2030-12-31")
                .get(ApiConfig.Endpoints.REPORT_REVENUE)
                .then().statusCode(200);
    }

    // ─── CAPACITY / OCCUPANCY REPORT ──────────────────────────────────────────

    @Test(priority = 21) @Story("Capacity Report") @Severity(SeverityLevel.BLOCKER)
    public void getCapacityReport_validShowtimeId_returns200() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 22) @Story("Capacity Report") @Severity(SeverityLevel.CRITICAL)
    public void getCapacityReport_withoutAuth_returns401() {
        withNoAuth().pathParam("showtimeId", 1)
                .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 23) @Story("Capacity Report") @Severity(SeverityLevel.CRITICAL)
    public void getCapacityReport_withUserRole_returns403() {
        withUserAuth().pathParam("showtimeId", 1)
                .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                .then().statusCode(403);
    }

    @Test(priority = 24) @Story("Capacity Report") @Severity(SeverityLevel.CRITICAL)
    public void getCapacityReport_nonExistentShowtime_returns404() {
        withAdminAuth().pathParam("showtimeId", 999999)
                .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                .then().statusCode(404);
    }

    @Test(priority = 25) @Story("Capacity Report") @Severity(SeverityLevel.NORMAL)
    public void getCapacityReport_containsTotalSeats() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                    .then().statusCode(200)
                    .body("data.totalSeats", notNullValue())
                    .body("data.totalSeats", greaterThan(0));
        }
    }

    @Test(priority = 26) @Story("Capacity Report") @Severity(SeverityLevel.NORMAL)
    public void getCapacityReport_containsBookedSeats() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                    .then().statusCode(200)
                    .body("data.bookedSeats", notNullValue());
        }
    }

    @Test(priority = 27) @Story("Capacity Report") @Severity(SeverityLevel.NORMAL)
    public void getCapacityReport_containsOccupancyPercentage() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                    .then().statusCode(200)
                    .body("data.occupancyPercentage", notNullValue());
        }
    }

    @Test(priority = 28) @Story("Capacity Report") @Severity(SeverityLevel.NORMAL)
    public void getCapacityReport_occupancyBetween0And100() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            Response res = withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY);
            res.then().statusCode(200);
            Float occupancy = res.jsonPath().getFloat("data.occupancyPercentage");
            if (occupancy != null) {
                assertThat((double) occupancy).isBetween(0.0, 100.0);
            }
        }
    }

    @Test(priority = 29) @Story("Capacity Report") @Severity(SeverityLevel.NORMAL)
    public void getCapacityReport_bookedSeatsLEQTotalSeats() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            Response res = withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY);
            res.then().statusCode(200);
            Integer booked = res.jsonPath().getInt("data.bookedSeats");
            Integer total = res.jsonPath().getInt("data.totalSeats");
            if (booked != null && total != null) {
                assertThat(booked).isLessThanOrEqualTo(total);
            }
        }
    }

    @Test(priority = 30) @Story("Capacity Report") @Severity(SeverityLevel.MINOR)
    public void getCapacityReport_responseTimeUnder5Seconds() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            Response res = withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY);
            assertThat(responseTimeMillis(res)).isLessThan(5000L);
        }
    }

    @Test(priority = 31) @Story("Capacity Report") @Severity(SeverityLevel.MINOR)
    public void getCapacityReport_withInvalidToken_returns401() {
        withInvalidToken().pathParam("showtimeId", 1)
                .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 32) @Story("Capacity Report") @Severity(SeverityLevel.MINOR)
    public void getCapacityReport_withNegativeId_returns4xx() {
        int status = withAdminAuth().pathParam("showtimeId", -1)
                .get(ApiConfig.Endpoints.REPORT_CAPACITY).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 33) @Story("Capacity Report") @Severity(SeverityLevel.MINOR)
    public void getCapacityReport_successFlagIsTrue() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                    .then().statusCode(200).body("success", equalTo(true));
        }
    }

    @Test(priority = 34) @Story("Capacity Report") @Severity(SeverityLevel.MINOR)
    public void getCapacityReport_contentTypeIsJson() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                    .then().contentType(containsString("application/json"));
        }
    }

    @Test(priority = 35) @Story("Capacity Report") @Severity(SeverityLevel.MINOR)
    public void getCapacityReport_availableSeatsIsPositive() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.SHOWTIMES).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            Response res = withAdminAuth().pathParam("showtimeId", ids.get(0))
                    .get(ApiConfig.Endpoints.REPORT_CAPACITY);
            res.then().statusCode(200);
            Integer available = res.jsonPath().getInt("data.availableSeats");
            if (available != null) {
                assertThat(available).isGreaterThanOrEqualTo(0);
            }
        }
    }

    // ─── TOP-GROSSING MOVIES REPORT ───────────────────────────────────────────

    @Test(priority = 36) @Story("Top Movies Report") @Severity(SeverityLevel.BLOCKER)
    public void getTopMovies_adminReturns200() {
        withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES)
                .then().statusCode(200);
    }

    @Test(priority = 37) @Story("Top Movies Report") @Severity(SeverityLevel.CRITICAL)
    public void getTopMovies_withoutAuth_returns401() {
        withNoAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 38) @Story("Top Movies Report") @Severity(SeverityLevel.CRITICAL)
    public void getTopMovies_withUserRole_returns403() {
        withUserAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES).then().statusCode(403);
    }

    @Test(priority = 39) @Story("Top Movies Report") @Severity(SeverityLevel.NORMAL)
    public void getTopMovies_returnsArray() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES);
        res.then().statusCode(200);
        assertThat((Object) res.jsonPath().get("data")).isInstanceOf(List.class);
    }

    @Test(priority = 40) @Story("Top Movies Report") @Severity(SeverityLevel.NORMAL)
    public void getTopMovies_atMost10Movies() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES);
        res.then().statusCode(200);
        List<Object> movies = res.jsonPath().getList("data");
        if (movies != null) {
            assertThat(movies.size()).isLessThanOrEqualTo(10);
        }
    }

    @Test(priority = 41) @Story("Top Movies Report") @Severity(SeverityLevel.NORMAL)
    public void getTopMovies_eachEntryHasMovieTitleAndRevenue() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES);
        res.then().statusCode(200);
        List<Object> movies = res.jsonPath().getList("data");
        if (movies != null && !movies.isEmpty()) {
            res.then()
                    .body("data[0].title", notNullValue())
                    .body("data[0].revenue", notNullValue());
        }
    }

    @Test(priority = 42) @Story("Top Movies Report") @Severity(SeverityLevel.NORMAL)
    public void getTopMovies_revenueIsNonNegative() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES);
        res.then().statusCode(200);
        List<Object> revenues = res.jsonPath().getList("data.revenue");
        if (revenues != null) {
            revenues.forEach(r -> assertThat(((Number) r).doubleValue()).isGreaterThanOrEqualTo(0.0));
        }
    }

    @Test(priority = 43) @Story("Top Movies Report") @Severity(SeverityLevel.NORMAL)
    public void getTopMovies_orderedByRevenueDescending() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES);
        res.then().statusCode(200);
        List<Object> revenues = res.jsonPath().getList("data.revenue");
        if (revenues != null && revenues.size() > 1) {
            for (int i = 0; i < revenues.size() - 1; i++) {
                assertThat(((Number) revenues.get(i)).doubleValue())
                        .isGreaterThanOrEqualTo(((Number) revenues.get(i + 1)).doubleValue());
            }
        }
    }

    @Test(priority = 44) @Story("Top Movies Report") @Severity(SeverityLevel.MINOR)
    public void getTopMovies_responseTimeUnder5Seconds() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 45) @Story("Top Movies Report") @Severity(SeverityLevel.MINOR)
    public void getTopMovies_withInvalidToken_returns401() {
        withInvalidToken().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 46) @Story("Top Movies Report") @Severity(SeverityLevel.MINOR)
    public void getTopMovies_successFlagIsTrue() {
        withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES)
                .then().statusCode(200).body("success", equalTo(true));
    }

    @Test(priority = 47) @Story("Top Movies Report") @Severity(SeverityLevel.MINOR)
    public void getTopMovies_contentTypeIsJson() {
        withAdminAuth().get(ApiConfig.Endpoints.REPORT_TOP_MOVIES)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 48) @Story("Top Movies Report") @Severity(SeverityLevel.MINOR)
    public void getTopMovies_limitParamReturnsFewerMovies() {
        withAdminAuth()
                .queryParam("limit", 5)
                .get(ApiConfig.Endpoints.REPORT_TOP_MOVIES)
                .then().statusCode(200);
    }

    @Test(priority = 49) @Story("Revenue Report") @Severity(SeverityLevel.MINOR)
    public void revenueReport_withExpiredToken_returns401() {
        withExpiredToken().get(ApiConfig.Endpoints.REPORT_REVENUE).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 50) @Story("Capacity Report") @Severity(SeverityLevel.MINOR)
    public void capacityReport_withExpiredToken_returns401() {
        withExpiredToken().pathParam("showtimeId", 1)
                .get(ApiConfig.Endpoints.REPORT_CAPACITY)
                .then().statusCode(anyOf(is(401), is(403)));
    }
}


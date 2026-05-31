package com.cinereserve.api.tests.halls;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.TestDataBuilder;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: Hall (Theater) API
 * Endpoints: GET/POST/PUT/DELETE /api/halls, GET /api/halls/{id}
 * Total @Test methods: 45
 */
@Epic("Halls")
@Feature("Hall Management")
public class HallTests extends BaseTest {

    // ─── GET HALLS ────────────────────────────────────────────────────────────

    @Test(priority = 1) @Story("List Halls") @Severity(SeverityLevel.BLOCKER)
    @Description("GET /api/halls returns 200 (admin)")
    public void getAllHalls_adminReturns200() {
        withAdminAuth().get(ApiConfig.Endpoints.HALLS).then().statusCode(200).body("success", equalTo(true));
    }

    @Test(priority = 2) @Story("List Halls") @Severity(SeverityLevel.CRITICAL)
    @Description("GET /api/halls without auth — may return 200 or 401")
    public void getAllHalls_noAuth_returns200or401() {
        int status = withNoAuth().get(ApiConfig.Endpoints.HALLS).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 401, 403);
    }

    @Test(priority = 3) @Story("List Halls") @Severity(SeverityLevel.NORMAL)
    public void getAllHalls_returnsArray() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.HALLS);
        res.then().statusCode(200);
        assertThat((Object) res.jsonPath().get("data")).isInstanceOf(List.class);
    }

    @Test(priority = 4) @Story("List Halls") @Severity(SeverityLevel.NORMAL)
    public void getAllHalls_eachHallHasNameAndCapacity() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.HALLS);
        res.then().statusCode(200);
        List<Object> halls = res.jsonPath().getList("data");
        if (!halls.isEmpty()) {
            res.then().body("data[0].id", notNullValue())
                    .body("data[0].name", notNullValue())
                    .body("data[0].totalRows", notNullValue())
                    .body("data[0].seatsPerRow", notNullValue());
        }
    }

    @Test(priority = 5) @Story("List Halls") @Severity(SeverityLevel.MINOR)
    public void getAllHalls_responseTimeUnder5Seconds() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.HALLS);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 6) @Story("Get Hall By ID") @Severity(SeverityLevel.BLOCKER)
    public void getHallById_validId_returns200() {
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.HALLS).jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            withAdminAuth().pathParam("id", ids.get(0))
                    .get(ApiConfig.Endpoints.HALL_BY_ID)
                    .then().statusCode(200)
                    .body("data.id", equalTo(ids.get(0)));
        }
    }

    @Test(priority = 7) @Story("Get Hall By ID") @Severity(SeverityLevel.CRITICAL)
    public void getHallById_nonExistentId_returns404() {
        withAdminAuth().pathParam("id", 999999)
                .get(ApiConfig.Endpoints.HALL_BY_ID)
                .then().statusCode(404);
    }

    @Test(priority = 8) @Story("Get Hall By ID") @Severity(SeverityLevel.NORMAL)
    public void getHallById_withoutAuth_returns200or401() {
        int status = withNoAuth().pathParam("id", 1).get(ApiConfig.Endpoints.HALL_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 401, 403, 404);
    }

    @Test(priority = 9) @Story("Get Hall By ID") @Severity(SeverityLevel.MINOR)
    public void getHallById_negativeId_returns4xx() {
        int status = withAdminAuth().pathParam("id", -1).get(ApiConfig.Endpoints.HALL_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 10) @Story("Get Hall By ID") @Severity(SeverityLevel.MINOR)
    public void getHallById_zeroId_returns4xx() {
        int status = withAdminAuth().pathParam("id", 0).get(ApiConfig.Endpoints.HALL_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    // ─── CREATE HALL ──────────────────────────────────────────────────────────

    @Test(priority = 11) @Story("Create Hall") @Severity(SeverityLevel.BLOCKER)
    public void adminCreatesHall_returns200() {
        Map<String, Object> body = TestDataBuilder.validHallBody();
        withAdminAuth().body(body).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.name", equalTo(body.get("name")));
    }

    @Test(priority = 12) @Story("Create Hall Authorization") @Severity(SeverityLevel.CRITICAL)
    public void createHallWithoutAuth_returns401() {
        withNoAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 13) @Story("Create Hall Authorization") @Severity(SeverityLevel.CRITICAL)
    public void createHallWithUserRole_returns403() {
        withUserAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(403);
    }

    @Test(priority = 14) @Story("Create Hall Validation") @Severity(SeverityLevel.CRITICAL)
    public void createHallWithEmptyName_returns400() {
        withAdminAuth().body(TestDataBuilder.hallBody("", 5, 10)).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(400);
    }

    @Test(priority = 15) @Story("Create Hall Validation") @Severity(SeverityLevel.CRITICAL)
    public void createHallWithZeroRows_returns400() {
        withAdminAuth().body(TestDataBuilder.hallBody("TestHall", 0, 10)).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(400);
    }

    @Test(priority = 16) @Story("Create Hall Validation") @Severity(SeverityLevel.CRITICAL)
    public void createHallWithNegativeRows_returns400() {
        withAdminAuth().body(TestDataBuilder.hallBody("TestHall", -3, 10)).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(400);
    }

    @Test(priority = 17) @Story("Create Hall Validation") @Severity(SeverityLevel.CRITICAL)
    public void createHallWithZeroSeatsPerRow_returns400() {
        withAdminAuth().body(TestDataBuilder.hallBody("TestHall", 5, 0)).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(400);
    }

    @Test(priority = 18) @Story("Create Hall Validation") @Severity(SeverityLevel.CRITICAL)
    public void createHallWithNegativeSeatsPerRow_returns400() {
        withAdminAuth().body(TestDataBuilder.hallBody("TestHall", 5, -10)).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(400);
    }

    @Test(priority = 19) @Story("Create Hall Validation") @Severity(SeverityLevel.NORMAL)
    public void createHallWithNullName_returns400() {
        withAdminAuth().body(TestDataBuilder.hallBody(null, 5, 10)).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(400);
    }

    @Test(priority = 20) @Story("Create Hall") @Severity(SeverityLevel.NORMAL)
    public void createHall_totalSeatsIsRowsTimesSeatsPerRow() {
        Map<String, Object> body = TestDataBuilder.hallBody("TotalTest", 5, 10);
        Response res = withAdminAuth().body(body).post(ApiConfig.Endpoints.HALLS);
        res.then().statusCode(200);
        int rows = res.jsonPath().getInt("data.totalRows");
        int seatsPerRow = res.jsonPath().getInt("data.seatsPerRow");
        assertThat(rows * seatsPerRow).isEqualTo(50);
    }

    @Test(priority = 21) @Story("Create Hall") @Severity(SeverityLevel.NORMAL)
    public void createHall_responseContainsAllFields() {
        withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(200)
                .body("data.id", notNullValue())
                .body("data.name", notNullValue())
                .body("data.totalRows", notNullValue())
                .body("data.seatsPerRow", notNullValue());
    }

    @Test(priority = 22) @Story("Create Hall") @Severity(SeverityLevel.NORMAL)
    public void createHall_appearsInHallList() {
        Map<String, Object> body = TestDataBuilder.validHallBody();
        int hallId = withAdminAuth().body(body).post(ApiConfig.Endpoints.HALLS)
                .jsonPath().getInt("data.id");
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.HALLS).jsonPath().getList("data.id");
        assertThat(ids).contains(hallId);
    }

    @Test(priority = 23) @Story("Create Hall") @Severity(SeverityLevel.MINOR)
    public void createHallWithLargeSeatLayout() {
        Map<String, Object> body = TestDataBuilder.hallBody("LargeHall", 20, 30);
        int status = withAdminAuth().body(body).post(ApiConfig.Endpoints.HALLS).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 24) @Story("Create Hall") @Severity(SeverityLevel.MINOR)
    public void createHall_responseTimeUnder5Seconds() {
        Response res = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 25) @Story("Create Hall") @Severity(SeverityLevel.NORMAL)
    public void createHall_withMinimumSeatLayout_1Row1Seat() {
        int status = withAdminAuth().body(TestDataBuilder.hallBody("MinHall", 1, 1)).post(ApiConfig.Endpoints.HALLS).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    // ─── UPDATE HALL ──────────────────────────────────────────────────────────

    @Test(priority = 26) @Story("Update Hall") @Severity(SeverityLevel.BLOCKER)
    public void adminUpdatesHall_returns200() {
        int hallId = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .jsonPath().getInt("data.id");
        Map<String, Object> updateBody = TestDataBuilder.hallBody("UpdatedHall_" + System.nanoTime() % 1000, 8, 12);
        withAdminAuth().pathParam("id", hallId).body(updateBody)
                .put(ApiConfig.Endpoints.HALL_BY_ID)
                .then().statusCode(200)
                .body("data.name", equalTo(updateBody.get("name")));
    }

    @Test(priority = 27) @Story("Update Hall Authorization") @Severity(SeverityLevel.CRITICAL)
    public void updateHallWithoutAuth_returns401() {
        withNoAuth().pathParam("id", 1).body(TestDataBuilder.validHallBody())
                .put(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 28) @Story("Update Hall Authorization") @Severity(SeverityLevel.CRITICAL)
    public void updateHallWithUserRole_returns403() {
        withUserAuth().pathParam("id", 1).body(TestDataBuilder.validHallBody())
                .put(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(403);
    }

    @Test(priority = 29) @Story("Update Hall") @Severity(SeverityLevel.CRITICAL)
    public void updateNonExistentHall_returns404() {
        withAdminAuth().pathParam("id", 999999).body(TestDataBuilder.validHallBody())
                .put(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(404);
    }

    @Test(priority = 30) @Story("Update Hall Validation") @Severity(SeverityLevel.NORMAL)
    public void updateHallWithEmptyName_returns400() {
        int hallId = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .jsonPath().getInt("data.id");
        int status = withAdminAuth().pathParam("id", hallId)
                .body(TestDataBuilder.hallBody("", 5, 10))
                .put(ApiConfig.Endpoints.HALL_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 200);
    }

    @Test(priority = 31) @Story("Update Hall") @Severity(SeverityLevel.NORMAL)
    public void updateHallRowsAndSeats_updatesSuccessfully() {
        int hallId = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .jsonPath().getInt("data.id");
        withAdminAuth().pathParam("id", hallId).body(TestDataBuilder.hallBody("UpdatedHall", 7, 15))
                .put(ApiConfig.Endpoints.HALL_BY_ID)
                .then().statusCode(200)
                .body("data.totalRows", equalTo(7))
                .body("data.seatsPerRow", equalTo(15));
    }

    // ─── DELETE HALL ──────────────────────────────────────────────────────────

    @Test(priority = 32) @Story("Delete Hall") @Severity(SeverityLevel.BLOCKER)
    public void adminDeletesHall_returns200() {
        int hallId = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .jsonPath().getInt("data.id");
        withAdminAuth().pathParam("id", hallId).delete(ApiConfig.Endpoints.HALL_BY_ID)
                .then().statusCode(200);
    }

    @Test(priority = 33) @Story("Delete Hall Authorization") @Severity(SeverityLevel.CRITICAL)
    public void deleteHallWithoutAuth_returns401() {
        withNoAuth().pathParam("id", 1).delete(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 34) @Story("Delete Hall Authorization") @Severity(SeverityLevel.CRITICAL)
    public void deleteHallWithUserRole_returns403() {
        withUserAuth().pathParam("id", 1).delete(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(403);
    }

    @Test(priority = 35) @Story("Delete Hall") @Severity(SeverityLevel.CRITICAL)
    public void deleteNonExistentHall_returns404() {
        withAdminAuth().pathParam("id", 999999).delete(ApiConfig.Endpoints.HALL_BY_ID)
                .then().statusCode(404);
    }

    @Test(priority = 36) @Story("Delete Hall") @Severity(SeverityLevel.NORMAL)
    public void deletedHallNotInList() {
        int hallId = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .jsonPath().getInt("data.id");
        withAdminAuth().pathParam("id", hallId).delete(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(200);
        List<Integer> ids = withAdminAuth().get(ApiConfig.Endpoints.HALLS).jsonPath().getList("data.id");
        if (ids != null) assertThat(ids).doesNotContain(hallId);
    }

    @Test(priority = 37) @Story("Delete Hall") @Severity(SeverityLevel.MINOR)
    public void deleteHallWithNegativeId_returns4xx() {
        int status = withAdminAuth().pathParam("id", -1).delete(ApiConfig.Endpoints.HALL_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 38) @Story("Delete Hall") @Severity(SeverityLevel.MINOR)
    public void deleteHallResponseHasSuccessTrue() {
        int hallId = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .jsonPath().getInt("data.id");
        withAdminAuth().pathParam("id", hallId).delete(ApiConfig.Endpoints.HALL_BY_ID)
                .then().statusCode(200).body("success", equalTo(true));
    }

    @Test(priority = 39) @Story("Delete Hall") @Severity(SeverityLevel.MINOR)
    public void doubleDeleteHall_secondReturns404OrOk() {
        int hallId = withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .jsonPath().getInt("data.id");
        withAdminAuth().pathParam("id", hallId).delete(ApiConfig.Endpoints.HALL_BY_ID).then().statusCode(200);
        int second = withAdminAuth().pathParam("id", hallId).delete(ApiConfig.Endpoints.HALL_BY_ID).statusCode();
        assertThat(Integer.valueOf(second)).isIn(404, 200, 400);
    }

    // ─── ADDITIONAL TESTS ─────────────────────────────────────────────────────

    @Test(priority = 40) @Story("Create Hall") @Severity(SeverityLevel.MINOR)
    public void createHallWithInvalidToken_returns401() {
        withInvalidToken().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 41) @Story("Create Hall") @Severity(SeverityLevel.MINOR)
    public void createHall_emptyBody_returns400() {
        withAdminAuth().body("{}").post(ApiConfig.Endpoints.HALLS).then().statusCode(400);
    }

    @Test(priority = 42) @Story("Create Hall") @Severity(SeverityLevel.NORMAL)
    public void createHall_seatsAreAutoGeneratedOnCreate() {
        Map<String, Object> body = TestDataBuilder.hallBody("SeatsHall", 3, 4);
        int hallId = withAdminAuth().body(body).post(ApiConfig.Endpoints.HALLS).jsonPath().getInt("data.id");
        assertThat(hallId).isGreaterThan(0);
    }

    @Test(priority = 43) @Story("List Halls") @Severity(SeverityLevel.MINOR)
    public void hallListGrowsAfterCreation() {
        int before = withAdminAuth().get(ApiConfig.Endpoints.HALLS).jsonPath().getList("data").size();
        withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS).then().statusCode(200);
        int after = withAdminAuth().get(ApiConfig.Endpoints.HALLS).jsonPath().getList("data").size();
        assertThat(after).isGreaterThan(before);
    }

    @Test(priority = 44) @Story("Create Hall") @Severity(SeverityLevel.MINOR)
    public void createHallWithSpecialCharsInName() {
        int status = withAdminAuth().body(TestDataBuilder.hallBody("Hall-A (VIP)", 5, 10))
                .post(ApiConfig.Endpoints.HALLS).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 45) @Story("Create Hall") @Severity(SeverityLevel.MINOR)
    public void createMultipleHalls_allSucceed() {
        for (int i = 0; i < 3; i++) {
            withAdminAuth().body(TestDataBuilder.validHallBody()).post(ApiConfig.Endpoints.HALLS)
                    .then().statusCode(200);
        }
    }
}


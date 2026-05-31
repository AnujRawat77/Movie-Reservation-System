package com.cinereserve.api.tests.users;

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
 * TEST SUITE: User Management API
 * Endpoints: GET /api/users/me, GET /api/users, PATCH /api/users/{id}/role
 * Total @Test methods: 40
 */
@Epic("Users")
@Feature("User Management")
public class UserTests extends BaseTest {

    // ─── GET MY PROFILE ───────────────────────────────────────────────────────

    @Test(priority = 1) @Story("Get My Profile") @Severity(SeverityLevel.BLOCKER)
    public void getMyProfile_returns200() {
        withUserAuth().get(ApiConfig.Endpoints.USER_ME)
                .then().statusCode(200).body("success", equalTo(true));
    }

    @Test(priority = 2) @Story("Get My Profile") @Severity(SeverityLevel.CRITICAL)
    public void getMyProfile_withoutAuth_returns401() {
        withNoAuth().get(ApiConfig.Endpoints.USER_ME).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 3) @Story("Get My Profile") @Severity(SeverityLevel.CRITICAL)
    public void getMyProfile_withInvalidToken_returns401() {
        withInvalidToken().get(ApiConfig.Endpoints.USER_ME).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 4) @Story("Get My Profile") @Severity(SeverityLevel.NORMAL)
    public void getMyProfile_containsIdNameEmailRole() {
        withUserAuth().get(ApiConfig.Endpoints.USER_ME)
                .then().statusCode(200)
                .body("data.id", notNullValue())
                .body("data.name", notNullValue())
                .body("data.email", notNullValue())
                .body("data.role", notNullValue());
    }

    @Test(priority = 5) @Story("Get My Profile") @Severity(SeverityLevel.CRITICAL)
    public void getMyProfile_doesNotExposePasswordHash() {
        Response res = withUserAuth().get(ApiConfig.Endpoints.USER_ME);
        res.then().statusCode(200);
        assertThat(res.body().asString())
                .doesNotContain("passwordHash")
                .doesNotContain("password_hash");
    }

    @Test(priority = 6) @Story("Get My Profile") @Severity(SeverityLevel.NORMAL)
    public void getMyProfile_roleIsUserForRegularUser() {
        withUserAuth().get(ApiConfig.Endpoints.USER_ME)
                .then().statusCode(200)
                .body("data.role", equalTo("USER"));
    }

    @Test(priority = 7) @Story("Get My Profile") @Severity(SeverityLevel.NORMAL)
    public void getMyProfile_adminCanGetOwnProfile() {
        withAdminAuth().get(ApiConfig.Endpoints.USER_ME)
                .then().statusCode(200)
                .body("data.role", equalTo("ADMIN"));
    }

    @Test(priority = 8) @Story("Get My Profile") @Severity(SeverityLevel.MINOR)
    public void getMyProfile_responseTimeUnder5Seconds() {
        Response res = withUserAuth().get(ApiConfig.Endpoints.USER_ME);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 9) @Story("Get My Profile") @Severity(SeverityLevel.MINOR)
    public void getMyProfile_contentTypeIsJson() {
        withUserAuth().get(ApiConfig.Endpoints.USER_ME)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 10) @Story("Get My Profile") @Severity(SeverityLevel.MINOR)
    public void getMyProfile_withExpiredToken_returns401() {
        withExpiredToken().get(ApiConfig.Endpoints.USER_ME).then().statusCode(anyOf(is(401), is(403)));
    }

    // ─── GET ALL USERS (ADMIN) ────────────────────────────────────────────────

    @Test(priority = 11) @Story("List All Users") @Severity(SeverityLevel.BLOCKER)
    public void adminGetAllUsers_returns200() {
        withAdminAuth().get(ApiConfig.Endpoints.USERS)
                .then().statusCode(200).body("success", equalTo(true));
    }

    @Test(priority = 12) @Story("List All Users") @Severity(SeverityLevel.CRITICAL)
    public void getAllUsers_withoutAuth_returns401() {
        withNoAuth().get(ApiConfig.Endpoints.USERS).then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 13) @Story("List All Users") @Severity(SeverityLevel.CRITICAL)
    public void getAllUsers_withUserRole_returns403() {
        withUserAuth().get(ApiConfig.Endpoints.USERS).then().statusCode(403);
    }

    @Test(priority = 14) @Story("List All Users") @Severity(SeverityLevel.NORMAL)
    public void adminGetAllUsers_returnsArray() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        res.then().statusCode(200);
        assertThat((Object) res.jsonPath().get("data")).isInstanceOf(List.class);
    }

    @Test(priority = 15) @Story("List All Users") @Severity(SeverityLevel.NORMAL)
    public void adminGetAllUsers_listNotEmpty() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        res.then().statusCode(200);
        List<Object> users = res.jsonPath().getList("data");
        assertThat(users).isNotEmpty();
    }

    @Test(priority = 16) @Story("List All Users") @Severity(SeverityLevel.NORMAL)
    public void adminGetAllUsers_eachUserHasIdEmailRole() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        res.then().statusCode(200);
        List<Object> users = res.jsonPath().getList("data");
        if (!users.isEmpty()) {
            res.then()
                    .body("data[0].id", notNullValue())
                    .body("data[0].email", notNullValue())
                    .body("data[0].role", notNullValue());
        }
    }

    @Test(priority = 17) @Story("List All Users") @Severity(SeverityLevel.CRITICAL)
    public void adminGetAllUsers_doesNotExposePasswordHashes() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        res.then().statusCode(200);
        assertThat(res.body().asString())
                .doesNotContain("passwordHash")
                .doesNotContain("password_hash");
    }

    @Test(priority = 18) @Story("List All Users") @Severity(SeverityLevel.MINOR)
    public void adminGetAllUsers_responseTimeUnder5Seconds() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 19) @Story("List All Users") @Severity(SeverityLevel.MINOR)
    public void adminGetAllUsers_contentTypeIsJson() {
        withAdminAuth().get(ApiConfig.Endpoints.USERS)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 20) @Story("List All Users") @Severity(SeverityLevel.MINOR)
    public void adminGetAllUsers_withInvalidToken_returns401() {
        withInvalidToken().get(ApiConfig.Endpoints.USERS).then().statusCode(anyOf(is(401), is(403)));
    }

    // ─── UPDATE USER ROLE (ADMIN) ─────────────────────────────────────────────

    @Test(priority = 21) @Story("Update User Role") @Severity(SeverityLevel.BLOCKER)
    public void adminPromotesUserToAdmin_returns200() {
        // Register new user to promote
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        Response regRes = withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER);
        regRes.then().statusCode(200);
        int userId = regRes.jsonPath().getInt("data.userId");

        withAdminAuth().pathParam("id", userId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(200)
                .body("data.role", equalTo("ADMIN"));
    }

    @Test(priority = 22) @Story("Update User Role") @Severity(SeverityLevel.BLOCKER)
    public void adminDemotesAdminToUser_returns200() {
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        int userId = withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER)
                .jsonPath().getInt("data.userId");

        // First promote
        withAdminAuth().pathParam("id", userId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(200);

        // Then demote
        withAdminAuth().pathParam("id", userId)
                .body(TestDataBuilder.roleBody("USER"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(200)
                .body("data.role", equalTo("USER"));
    }

    @Test(priority = 23) @Story("Update User Role Authorization") @Severity(SeverityLevel.CRITICAL)
    public void updateUserRole_withoutAuth_returns401() {
        withNoAuth().pathParam("id", createdUserId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 24) @Story("Update User Role Authorization") @Severity(SeverityLevel.CRITICAL)
    public void updateUserRole_withUserRole_returns403() {
        withUserAuth().pathParam("id", createdUserId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(403);
    }

    @Test(priority = 25) @Story("Update User Role Validation") @Severity(SeverityLevel.CRITICAL)
    public void updateUserRole_withInvalidRole_returns400() {
        withAdminAuth().pathParam("id", createdUserId)
                .body(TestDataBuilder.roleBody("SUPERUSER"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(400);
    }

    @Test(priority = 26) @Story("Update User Role Validation") @Severity(SeverityLevel.CRITICAL)
    public void updateUserRole_withEmptyRole_returns400() {
        withAdminAuth().pathParam("id", createdUserId)
                .body(TestDataBuilder.roleBody(""))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(400);
    }

    @Test(priority = 27) @Story("Update User Role Validation") @Severity(SeverityLevel.CRITICAL)
    public void updateUserRole_withNullRole_returns400() {
        withAdminAuth().pathParam("id", createdUserId)
                .body(TestDataBuilder.roleBody(null))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(400);
    }

    @Test(priority = 28) @Story("Update User Role") @Severity(SeverityLevel.CRITICAL)
    public void updateUserRole_nonExistentUser_returns404() {
        withAdminAuth().pathParam("id", 999999)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(404);
    }

    @Test(priority = 29) @Story("Update User Role") @Severity(SeverityLevel.NORMAL)
    public void updateUserRole_responseHasSuccessTrue() {
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        int userId = withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER)
                .jsonPath().getInt("data.userId");
        withAdminAuth().pathParam("id", userId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(200).body("success", equalTo(true));
    }

    @Test(priority = 30) @Story("Update User Role") @Severity(SeverityLevel.NORMAL)
    public void updateUserRole_roleChangePersists() {
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        Response regRes = withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER);
        int userId = regRes.jsonPath().getInt("data.userId");
        String token = regRes.jsonPath().getString("data.token");

        withAdminAuth().pathParam("id", userId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(200);

        // Login again — token should reflect new role
        Response loginRes = withNoAuth()
                .body(TestDataBuilder.validLoginBody((String) regBody.get("email"), (String) regBody.get("password")))
                .post(ApiConfig.Endpoints.LOGIN);
        if (loginRes.statusCode() == 200) {
            assertThat(loginRes.jsonPath().getString("data.role")).isEqualTo("ADMIN");
        }
    }

    @Test(priority = 31) @Story("Update User Role") @Severity(SeverityLevel.NORMAL)
    public void updateUserRole_withInvalidToken_returns401() {
        withInvalidToken().pathParam("id", createdUserId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 32) @Story("Update User Role") @Severity(SeverityLevel.MINOR)
    public void updateUserRole_responseTimeUnder5Seconds() {
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        int userId = withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER)
                .jsonPath().getInt("data.userId");
        Response res = withAdminAuth().pathParam("id", userId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 33) @Story("Update User Role") @Severity(SeverityLevel.MINOR)
    public void updateUserRole_emptyBody_returns400() {
        withAdminAuth().pathParam("id", createdUserId)
                .body("{}")
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(400);
    }

    @Test(priority = 34) @Story("Update User Role") @Severity(SeverityLevel.MINOR)
    public void updateUserRole_contentTypeIsJson() {
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        int userId = withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER)
                .jsonPath().getInt("data.userId");
        withAdminAuth().pathParam("id", userId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 35) @Story("Get My Profile") @Severity(SeverityLevel.NORMAL)
    public void getMyProfile_idMatchesRegisteredUserId() {
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        Response regRes = withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER);
        regRes.then().statusCode(200);
        int userId = regRes.jsonPath().getInt("data.userId");
        String token = regRes.jsonPath().getString("data.token");

        withBearerToken(token).get(ApiConfig.Endpoints.USER_ME)
                .then().statusCode(200)
                .body("data.id", equalTo(userId));
    }

    @Test(priority = 36) @Story("Get My Profile") @Severity(SeverityLevel.NORMAL)
    public void getMyProfile_emailMatchesRegisteredEmail() {
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        Response regRes = withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER);
        regRes.then().statusCode(200);
        String token = regRes.jsonPath().getString("data.token");
        String email = (String) regBody.get("email");

        withBearerToken(token).get(ApiConfig.Endpoints.USER_ME)
                .then().statusCode(200)
                .body("data.email", equalTo(email));
    }

    @Test(priority = 37) @Story("List All Users") @Severity(SeverityLevel.NORMAL)
    public void adminGetAllUsers_adminAccountIncluded() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        res.then().statusCode(200);
        List<String> emails = res.jsonPath().getList("data.email");
        if (emails != null) {
            assertThat(emails).contains(ApiConfig.ADMIN_EMAIL);
        }
    }

    @Test(priority = 38) @Story("List All Users") @Severity(SeverityLevel.NORMAL)
    public void adminGetAllUsers_roleFieldOnlyAdminOrUser() {
        Response res = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        res.then().statusCode(200);
        List<String> roles = res.jsonPath().getList("data.role");
        if (roles != null) {
            roles.forEach(role -> assertThat(role).isIn("USER", "ADMIN"));
        }
    }

    @Test(priority = 39) @Story("Update User Role") @Severity(SeverityLevel.MINOR)
    public void updateUserRole_withNegativeUserId_returns4xx() {
        int status = withAdminAuth().pathParam("id", -1)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 40) @Story("Update User Role") @Severity(SeverityLevel.MINOR)
    public void updateUserRole_userPromotedSuccessfullyVerifyInUserList() {
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        int userId = withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER)
                .jsonPath().getInt("data.userId");
        withAdminAuth().pathParam("id", userId)
                .body(TestDataBuilder.roleBody("ADMIN"))
                .patch(ApiConfig.Endpoints.USER_ROLE)
                .then().statusCode(200);

        Response allUsers = withAdminAuth().get(ApiConfig.Endpoints.USERS);
        List<Map<String, Object>> users = allUsers.jsonPath().getList("data");
        if (users != null) {
            users.stream()
                    .filter(u -> userId == (int) u.get("id"))
                    .findFirst()
                    .ifPresent(u -> assertThat(u.get("role")).isEqualTo("ADMIN"));
        }
    }
}


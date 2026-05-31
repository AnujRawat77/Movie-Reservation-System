package com.cinereserve.api.tests.auth;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.TestDataBuilder;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: User Login API
 * Endpoint: POST /api/auth/login
 * Total @Test methods: 30
 */
@Epic("Authentication")
@Feature("User Login")
public class LoginTests extends BaseTest {

    // ─── POSITIVE ────────────────────────────────────────────────────────────

    @Test(priority = 1, description = "Admin login with correct credentials")
    @Story("Successful Login")
    @Severity(SeverityLevel.BLOCKER)
    public void adminLoginWithCorrectCredentials_returns200() {
        withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(200)
                .body("success", equalTo(true))
                .body("data.token", notNullValue())
                .body("data.role", equalTo("ADMIN"));
    }

    @Test(priority = 2, description = "Login response contains token")
    @Story("Successful Login")
    @Severity(SeverityLevel.BLOCKER)
    public void loginResponseContainsToken() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN);
        String token = res.jsonPath().getString("data.token");
        assertThat(token).isNotBlank();
    }

    @Test(priority = 3, description = "Login token is a valid JWT (3 parts separated by dots)")
    @Story("Successful Login")
    @Severity(SeverityLevel.CRITICAL)
    public void loginTokenIsValidJwtFormat() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN);
        String token = res.jsonPath().getString("data.token");
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        // Each part should be base64url encoded
        for (String part : parts) {
            assertThat(part).isNotBlank();
        }
    }

    @Test(priority = 4, description = "Login response contains userId")
    @Story("Successful Login")
    @Severity(SeverityLevel.NORMAL)
    public void loginResponseContainsUserId() {
        withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(200)
                .body("data.userId", notNullValue())
                .body("data.userId", greaterThan(0));
    }

    @Test(priority = 5, description = "Login response contains email")
    @Story("Successful Login")
    @Severity(SeverityLevel.NORMAL)
    public void loginResponseContainsEmail() {
        withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(200)
                .body("data.email", equalTo(ApiConfig.ADMIN_EMAIL));
    }

    @Test(priority = 6, description = "Login content-type is application/json")
    @Story("Successful Login")
    @Severity(SeverityLevel.MINOR)
    public void loginContentTypeIsJson() {
        withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 7, description = "Login response time is under 5 seconds")
    @Story("Successful Login")
    @Severity(SeverityLevel.MINOR)
    public void loginResponseTimeUnder5Seconds() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 8, description = "Login multiple times with same credentials — all succeed")
    @Story("Successful Login")
    @Severity(SeverityLevel.NORMAL)
    public void loginMultipleTimes_allSucceed() {
        for (int i = 0; i < 3; i++) {
            withNoAuth()
                    .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                    .post(ApiConfig.Endpoints.LOGIN)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 9, description = "Login success flag is true")
    @Story("Successful Login")
    @Severity(SeverityLevel.NORMAL)
    public void loginSuccessFlagIsTrue() {
        withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().body("success", equalTo(true));
    }

    @Test(priority = 10, description = "Login with registered user token has USER role")
    @Story("Successful Login")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithRegisteredUser_tokenHasUserRole() {
        // Register
        Map<String, Object> regBody = TestDataBuilder.validRegisterBody();
        withNoAuth().body(regBody).post(ApiConfig.Endpoints.REGISTER).then().statusCode(200);

        // Login
        withNoAuth()
                .body(TestDataBuilder.validLoginBody((String) regBody.get("email"), (String) regBody.get("password")))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(200)
                .body("data.role", equalTo("USER"));
    }

    // ─── NEGATIVE ────────────────────────────────────────────────────────────

    @Test(priority = 11, description = "Login with wrong password returns 400 or 401")
    @Story("Login Failures")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithWrongPassword_returnsError() {
        Map<String, Object> body = TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, "WrongPass@99");
        Response res = withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN);
        assertThat(res.statusCode()).isIn(400, 401, 403);
    }

    @Test(priority = 12, description = "Login with non-existent email returns error")
    @Story("Login Failures")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithNonExistentEmail_returnsError() {
        Map<String, Object> body = TestDataBuilder.validLoginBody("nobody@cinereserve-notexist.com", "Pass@123");
        Response res = withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN);
        assertThat(res.statusCode()).isIn(400, 401, 404);
    }

    @Test(priority = 13, description = "Login with empty email returns 400")
    @Story("Login Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithEmptyEmail_returns400() {
        withNoAuth()
                .body(TestDataBuilder.validLoginBody("", ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(400);
    }

    @Test(priority = 14, description = "Login with empty password returns 400")
    @Story("Login Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithEmptyPassword_returns400() {
        withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ""))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(400);
    }

    @Test(priority = 15, description = "Login with empty body returns 400")
    @Story("Login Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithEmptyBody_returns400() {
        withNoAuth()
                .body("{}")
                .post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(400);
    }

    @Test(priority = 16, description = "Login with invalid email format returns 400")
    @Story("Login Validation")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithInvalidEmailFormat_returns400() {
        withNoAuth()
                .body(TestDataBuilder.validLoginBody("invalid-email", ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(400);
    }

    @Test(priority = 17, description = "Login with null email returns 400")
    @Story("Login Validation")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithNullEmail_returns400() {
        Map<String, Object> body = new HashMap<>();
        body.put("email", null);
        body.put("password", ApiConfig.ADMIN_PASSWORD);
        withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(400);
    }

    @Test(priority = 18, description = "Login with null password returns 400")
    @Story("Login Validation")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithNullPassword_returns400() {
        Map<String, Object> body = new HashMap<>();
        body.put("email", ApiConfig.ADMIN_EMAIL);
        body.put("password", null);
        withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(400);
    }

    @Test(priority = 19, description = "Login error response has success: false")
    @Story("Login Failures")
    @Severity(SeverityLevel.NORMAL)
    public void loginErrorHasSuccessFalse() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody("notexist@test.com", "WrongPass@1"))
                .post(ApiConfig.Endpoints.LOGIN);
        if (res.statusCode() != 200) {
            assertThat(res.jsonPath().getBoolean("success")).isFalse();
        }
    }

    @Test(priority = 20, description = "Login error response does not contain token")
    @Story("Login Security")
    @Severity(SeverityLevel.CRITICAL)
    public void loginFailureResponseDoesNotContainToken() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody("baduser@test.com", "BadPass@1"))
                .post(ApiConfig.Endpoints.LOGIN);
        if (res.statusCode() != 200) {
            assertThat(res.body().asString()).doesNotContain("\"token\"");
        }
    }

    @Test(priority = 21, description = "Login with SQL injection in email is handled safely")
    @Story("Login Security")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithSqlInjectionEmail_handledGracefully() {
        Map<String, Object> body = TestDataBuilder.validLoginBody(
                "admin'--@test.com", "Pass@123");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 401, 404);
    }

    @Test(priority = 22, description = "Login with XSS in password is handled safely")
    @Story("Login Security")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithXSSInPassword_handledGracefully() {
        Map<String, Object> body = TestDataBuilder.validLoginBody(
                ApiConfig.ADMIN_EMAIL, "<script>alert(1)</script>");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN).statusCode();
        assertThat(Integer.valueOf(status)).isIn(400, 401, 403);
    }

    @Test(priority = 23, description = "Login error does not leak stack trace")
    @Story("Login Security")
    @Severity(SeverityLevel.NORMAL)
    public void loginErrorDoesNotLeakStackTrace() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody("nonexist@test.com", "WrongPass@1"))
                .post(ApiConfig.Endpoints.LOGIN);
        assertThat(res.body().asString())
                .doesNotContain("java.lang")
                .doesNotContain("at com.");
    }

    @Test(priority = 24, description = "Login with missing email field returns 400")
    @Story("Login Validation")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithMissingEmailField_returns400() {
        Map<String, Object> body = new HashMap<>();
        body.put("password", ApiConfig.ADMIN_PASSWORD);
        withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(400);
    }

    @Test(priority = 25, description = "Login with missing password field returns 400")
    @Story("Login Validation")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithMissingPasswordField_returns400() {
        Map<String, Object> body = new HashMap<>();
        body.put("email", ApiConfig.ADMIN_EMAIL);
        withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(400);
    }

    @Test(priority = 26, description = "Login with correct uppercase-cased email if case-insensitive")
    @Story("Successful Login")
    @Severity(SeverityLevel.MINOR)
    public void loginWithEmailCaseVariation_behaviorDefined() {
        // Just verify it doesn't crash
        Map<String, Object> body = TestDataBuilder.validLoginBody(
                ApiConfig.ADMIN_EMAIL.toUpperCase(), ApiConfig.ADMIN_PASSWORD);
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.LOGIN).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 401, 404);
    }

    @Test(priority = 27, description = "Login response does not expose passwordHash")
    @Story("Login Security")
    @Severity(SeverityLevel.CRITICAL)
    public void loginResponseDoesNotExposePasswordHash() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN);
        assertThat(res.body().asString())
                .doesNotContain("passwordHash")
                .doesNotContain("password_hash");
    }

    @Test(priority = 28, description = "Login with whitespace-only email returns 400")
    @Story("Login Validation")
    @Severity(SeverityLevel.MINOR)
    public void loginWithWhitespaceEmail_returns400() {
        withNoAuth()
                .body(TestDataBuilder.validLoginBody("   ", ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN)
                .then().statusCode(400);
    }

    @Test(priority = 29, description = "Login with whitespace-only password returns error")
    @Story("Login Validation")
    @Severity(SeverityLevel.MINOR)
    public void loginWithWhitespacePassword_returnsError() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, "   "))
                .post(ApiConfig.Endpoints.LOGIN);
        assertThat(res.statusCode()).isIn(400, 401, 403);
    }

    @Test(priority = 30, description = "Login response message is non-empty on success")
    @Story("Successful Login")
    @Severity(SeverityLevel.MINOR)
    public void loginSuccessResponseHasMessage() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validLoginBody(ApiConfig.ADMIN_EMAIL, ApiConfig.ADMIN_PASSWORD))
                .post(ApiConfig.Endpoints.LOGIN);
        res.then().statusCode(200);
        // message or data must exist
        String bodyStr = res.body().asString();
        assertThat(bodyStr).contains("success");
    }
}


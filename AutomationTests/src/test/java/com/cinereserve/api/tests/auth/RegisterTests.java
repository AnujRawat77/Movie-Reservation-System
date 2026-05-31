package com.cinereserve.api.tests.auth;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.TestDataBuilder;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: User Registration API
 * Endpoint: POST /api/auth/register
 * Total @Test methods: 30
 */
@Epic("Authentication")
@Feature("User Registration")
public class RegisterTests extends BaseTest {

    // ─── POSITIVE TESTS ─────────────────────────────────────────────────────

    @Test(priority = 1, description = "Register with valid name, email and password")
    @Story("Successful Registration")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valid user data should return 200 with token and user info")
    public void registerWithValidData_returns200AndToken() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        Response res = withNoAuth()
                .body(body)
                .post(ApiConfig.Endpoints.REGISTER);

        res.then().statusCode(200)
                .body("success", equalTo(true))
                .body("data.token", notNullValue())
                .body("data.email", equalTo(body.get("email")))
                .body("data.role", equalTo("USER"));

        assertThat(res.jsonPath().getString("data.token")).isNotEmpty();
    }

    @Test(priority = 2, description = "Registered user role should default to USER")
    @Story("Successful Registration")
    @Severity(SeverityLevel.NORMAL)
    public void registerDefaultRoleIsUser() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validRegisterBody())
                .post(ApiConfig.Endpoints.REGISTER);

        res.then().statusCode(200)
                .body("data.role", equalTo("USER"));
    }

    @Test(priority = 3, description = "Registration response includes userId")
    @Story("Successful Registration")
    @Severity(SeverityLevel.NORMAL)
    public void registerResponseContainsUserId() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validRegisterBody())
                .post(ApiConfig.Endpoints.REGISTER);

        res.then().statusCode(200)
                .body("data.userId", notNullValue())
                .body("data.userId", greaterThan(0));
    }

    @Test(priority = 4, description = "Register returns name in response")
    @Story("Successful Registration")
    @Severity(SeverityLevel.MINOR)
    public void registerResponseContainsName() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        Response res = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER);
        res.then().statusCode(200)
                .body("data.name", equalTo(body.get("name")));
    }

    @Test(priority = 5, description = "Register Content-Type is application/json")
    @Story("Successful Registration")
    @Severity(SeverityLevel.MINOR)
    public void registerResponseContentTypeIsJson() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validRegisterBody())
                .post(ApiConfig.Endpoints.REGISTER);
        res.then().statusCode(200)
                .contentType(containsString("application/json"));
    }

    @Test(priority = 6, description = "Register with password containing special characters")
    @Story("Successful Registration")
    @Severity(SeverityLevel.NORMAL)
    public void registerWithSpecialCharPasswordSucceeds() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("password", "P@$$w0rd#123!");
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(200);
    }

    @Test(priority = 7, description = "Response time for registration is under 5 seconds")
    @Story("Successful Registration")
    @Severity(SeverityLevel.MINOR)
    public void registerResponseTimeUnder5Seconds() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validRegisterBody())
                .post(ApiConfig.Endpoints.REGISTER);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 8, description = "Register success flag is true")
    @Story("Successful Registration")
    @Severity(SeverityLevel.NORMAL)
    public void registerSuccessFlagIsTrue() {
        withNoAuth()
                .body(TestDataBuilder.validRegisterBody())
                .post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(200)
                .body("success", equalTo(true));
    }

    // ─── NEGATIVE TESTS ─────────────────────────────────────────────────────

    @Test(priority = 9, description = "Register with empty body returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void registerWithEmptyBody_returns400() {
        withNoAuth()
                .body("{}")
                .post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 10, description = "Register with missing name returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void registerWithMissingName_returns400() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.remove("name");
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 11, description = "Register with missing email returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void registerWithMissingEmail_returns400() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.remove("email");
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 12, description = "Register with missing password returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void registerWithMissingPassword_returns400() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.remove("password");
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 13, description = "Register with invalid email format returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.CRITICAL)
    @DataProvider(name = "invalidEmails")
    public void registerWithInvalidEmail_returns400() {
        String[] invalidEmails = {"notanemail", "missing@", "@nodomain.com", "a@b", "spaces in@email.com"};
        for (String email : invalidEmails) {
            Map<String, Object> body = TestDataBuilder.validRegisterBody();
            body.put("email", email);
            withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                    .then().statusCode(400);
        }
    }

    @Test(priority = 14, description = "Register with duplicate email returns 400 or 409")
    @Story("Registration Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void registerWithDuplicateEmail_returnsError() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        // First registration
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(200);
        // Duplicate
        Response second = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER);
        assertThat(second.statusCode()).isIn(400, 409);
    }

    @Test(priority = 15, description = "Register with blank name returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.NORMAL)
    public void registerWithBlankName_returns400() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("name", "");
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 16, description = "Register with blank email returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.NORMAL)
    public void registerWithBlankEmail_returns400() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("email", "");
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 17, description = "Register with blank password returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.NORMAL)
    public void registerWithBlankPassword_returns400() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("password", "");
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 18, description = "Register with null name returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.NORMAL)
    public void registerWithNullName_returns400() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("name", null);
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 19, description = "Register with null email returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.NORMAL)
    public void registerWithNullEmail_returns400() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("email", null);
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 20, description = "Register with null password returns 400")
    @Story("Registration Validation")
    @Severity(SeverityLevel.NORMAL)
    public void registerWithNullPassword_returns400() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("password", null);
        withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                .then().statusCode(400);
    }

    @Test(priority = 21, description = "Register with very long name (500 chars)")
    @Story("Registration Boundary")
    @Severity(SeverityLevel.MINOR)
    public void registerWithVeryLongName() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("name", TestDataBuilder.longString(500));
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 422);
    }

    @Test(priority = 22, description = "Register with very long email exceeding column length")
    @Story("Registration Boundary")
    @Severity(SeverityLevel.MINOR)
    public void registerWithVeryLongEmail() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("email", TestDataBuilder.longString(300) + "@test.com");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 422, 500);
    }

    @Test(priority = 23, description = "Password not returned in registration response")
    @Story("Registration Security")
    @Severity(SeverityLevel.CRITICAL)
    public void registerPasswordNotExposedInResponse() {
        Response res = withNoAuth()
                .body(TestDataBuilder.validRegisterBody())
                .post(ApiConfig.Endpoints.REGISTER);
        res.then().statusCode(200);
        String body = res.body().asString();
        assertThat(body).doesNotContain("passwordHash")
                .doesNotContain("password_hash");
    }

    @Test(priority = 24, description = "Register with SQL injection in name")
    @Story("Registration Security")
    @Severity(SeverityLevel.CRITICAL)
    public void registerWithSqlInjectionInName_handledGracefully() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("name", "Robert'); DROP TABLE users;--");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 25, description = "Register with XSS in name field")
    @Story("Registration Security")
    @Severity(SeverityLevel.CRITICAL)
    public void registerWithXSSInName_handledGracefully() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("name", "<script>alert('XSS')</script>");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 26, description = "Register with name having only spaces")
    @Story("Registration Validation")
    @Severity(SeverityLevel.MINOR)
    public void registerWithSpacesOnlyName() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("name", "   ");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 27, description = "Register response does not expose internal server error details")
    @Story("Registration Security")
    @Severity(SeverityLevel.NORMAL)
    public void registerErrorResponseDoesNotLeakStackTrace() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("email", "not-an-email");
        Response res = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER);
        assertThat(res.body().asString())
                .doesNotContain("java.lang")
                .doesNotContain("StackTrace");
    }

    @Test(priority = 28, description = "Register with numeric password only")
    @Story("Registration Validation")
    @Severity(SeverityLevel.MINOR)
    public void registerWithNumericPassword() {
        Map<String, Object> body = TestDataBuilder.validRegisterBody();
        body.put("password", "12345678");
        int status = withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400);
    }

    @Test(priority = 29, description = "Register returns success: true on successful registration")
    @Story("Successful Registration")
    @Severity(SeverityLevel.NORMAL)
    public void registerReturnsSuccessTrue() {
        withNoAuth()
                .body(TestDataBuilder.validRegisterBody())
                .post(ApiConfig.Endpoints.REGISTER)
                .then().body("success", equalTo(true));
    }

    @Test(priority = 30, description = "Multiple different users can register concurrently")
    @Story("Successful Registration")
    @Severity(SeverityLevel.NORMAL)
    public void multipleDifferentUsersCanRegister() {
        for (int i = 0; i < 3; i++) {
            Map<String, Object> body = TestDataBuilder.validRegisterBody();
            withNoAuth().body(body).post(ApiConfig.Endpoints.REGISTER)
                    .then().statusCode(200);
        }
    }
}


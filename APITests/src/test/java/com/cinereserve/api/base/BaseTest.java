package com.cinereserve.api.base;

import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.AuthUtil;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;


import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

/**
 * Base test class — configures RestAssured, logging, Allure, and token helpers.
 */
public abstract class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);

    protected static String adminToken;
    protected static String userToken;
    protected static int    createdUserId;

    protected static RequestSpecification requestSpec;
    protected static RequestSpecification authRequestSpec;
    protected static RequestSpecification adminRequestSpec;
    protected static ResponseSpecification responseSpec200;
    protected static ResponseSpecification responseSpec201;
    protected static ResponseSpecification responseSpec400;
    protected static ResponseSpecification responseSpec401;
    protected static ResponseSpecification responseSpec403;
    protected static ResponseSpecification responseSpec404;
    protected static ResponseSpecification responseSpec409;

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        log.info("===== Global Suite Setup =====");
        RestAssured.baseURI = ApiConfig.BASE_URL;
        RestAssured.useRelaxedHTTPSValidation();

        // Base spec — no auth
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(ApiConfig.BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.ALL)
                .build();

        // Response specs
        responseSpec200 = new ResponseSpecBuilder().expectStatusCode(200).build();
        responseSpec201 = new ResponseSpecBuilder().expectStatusCode(201).build();
        responseSpec400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        responseSpec401 = new ResponseSpecBuilder().expectStatusCode(401).build();
        responseSpec403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        responseSpec404 = new ResponseSpecBuilder().expectStatusCode(404).build();
        responseSpec409 = new ResponseSpecBuilder().expectStatusCode(409).build();

        // Obtain admin token
        try {
            adminToken = AuthUtil.getAdminToken();
            log.info("Admin token obtained successfully");
        } catch (Exception e) {
            log.warn("Could not obtain admin token (server may be offline): {}", e.getMessage());
            adminToken = "INVALID_TOKEN";
        }

        // Build admin spec
        adminRequestSpec = new RequestSpecBuilder()
                .setBaseUri(ApiConfig.BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + adminToken)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.ALL)
                .build();

        // Register a regular test user and obtain their token
        try {
            String[] userCreds = AuthUtil.registerAndGetToken("testuser_" + System.currentTimeMillis());
            userToken     = userCreds[0];
            createdUserId = Integer.parseInt(userCreds[1]);
            authRequestSpec = new RequestSpecBuilder()
                    .setBaseUri(ApiConfig.BASE_URL)
                    .setContentType(ContentType.JSON)
                    .setAccept(ContentType.JSON)
                    .addHeader("Authorization", "Bearer " + userToken)
                    .addFilter(new AllureRestAssured())
                    .log(LogDetail.ALL)
                    .build();
            log.info("Test user registered, userId={}", createdUserId);
        } catch (Exception e) {
            log.warn("Could not register test user: {}", e.getMessage());
            userToken       = "INVALID_USER_TOKEN";
            authRequestSpec = requestSpec;
        }
    }

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        log.info("----- Setting up test class: {} -----", getClass().getSimpleName());
    }

    // ─── convenience builders ───────────────────────────────────────────

    protected RequestSpecification withNoAuth() {
        return given().spec(requestSpec);
    }

    protected RequestSpecification withAdminAuth() {
        return given().spec(adminRequestSpec);
    }

    protected RequestSpecification withUserAuth() {
        return given().spec(authRequestSpec);
    }

    protected RequestSpecification withBearerToken(String token) {
        return given().spec(requestSpec).header("Authorization", "Bearer " + token);
    }

    protected RequestSpecification withInvalidToken() {
        return given().spec(requestSpec).header("Authorization", "Bearer invalid.jwt.token");
    }

    protected RequestSpecification withExpiredToken() {
        // A well-formed but expired JWT (exp=1)
        String expired = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiZXhwIjoxfQ." +
                "someSignature";
        return given().spec(requestSpec).header("Authorization", "Bearer " + expired);
    }

    protected long responseTimeMillis(io.restassured.response.Response response) {
        return response.getTimeIn(TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a showtime safely, retrying up to {@code maxAttempts} times if there is a conflict.
     * Returns the showtime ID, or null if all attempts fail.
     */
    protected Integer createShowtimeSafe(int movieId, int hallId) {
        for (int attempt = 0; attempt < 10; attempt++) {
            Object id = withAdminAuth()
                    .body(com.cinereserve.api.utils.TestDataBuilder.validShowtimeBody(movieId, hallId))
                    .post(ApiConfig.Endpoints.SHOWTIMES)
                    .jsonPath().get("data.id");
            if (id instanceof Number) return ((Number) id).intValue();
        }
        log.warn("createShowtimeSafe: all 10 attempts failed for movieId={} hallId={}", movieId, hallId);
        return null;
    }
}


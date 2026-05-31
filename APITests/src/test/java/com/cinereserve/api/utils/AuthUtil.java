package com.cinereserve.api.utils;

import com.cinereserve.api.config.ApiConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Authentication utility — token management for tests.
 */
public class AuthUtil {

    public static String getAdminToken() {
        Map<String, String> body = new HashMap<>();
        body.put("email", ApiConfig.ADMIN_EMAIL);
        body.put("password", ApiConfig.ADMIN_PASSWORD);

        Response response = given()
                .baseUri(ApiConfig.BASE_URL)
                .contentType(ContentType.JSON)
                .body(body)
                .post(ApiConfig.Endpoints.LOGIN);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Admin login failed: " + response.statusCode()
                    + " " + response.body().asString());
        }
        return response.jsonPath().getString("data.token");
    }

    public static String getUserToken(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Response response = given()
                .baseUri(ApiConfig.BASE_URL)
                .contentType(ContentType.JSON)
                .body(body)
                .post(ApiConfig.Endpoints.LOGIN);

        if (response.statusCode() != 200) {
            throw new RuntimeException("User login failed: " + response.statusCode()
                    + " " + response.body().asString());
        }
        return response.jsonPath().getString("data.token");
    }

    /**
     * Registers a new user and returns [token, userId].
     */
    public static String[] registerAndGetToken(String usernameSuffix) {
        String email    = usernameSuffix + "@cinereserve-test.com";
        String password = "Test@12345";
        String name     = "Test User " + usernameSuffix;

        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);

        Response response = given()
                .baseUri(ApiConfig.BASE_URL)
                .contentType(ContentType.JSON)
                .body(body)
                .post(ApiConfig.Endpoints.REGISTER);

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            // Maybe already registered — try logging in
            return new String[]{getUserToken(email, password), "0"};
        }
        String token  = response.jsonPath().getString("data.token");
        String userId = String.valueOf(response.jsonPath().getInt("data.userId"));
        return new String[]{token, userId};
    }

    public static String buildBearerHeader(String token) {
        return "Bearer " + token;
    }
}


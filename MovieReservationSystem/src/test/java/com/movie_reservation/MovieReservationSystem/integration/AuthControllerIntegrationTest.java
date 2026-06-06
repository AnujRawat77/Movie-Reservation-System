package com.movie_reservation.MovieReservationSystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void register_withValidData_returns200() throws Exception {
        String uniqueEmail = "inttest_" + System.currentTimeMillis() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Integration User",
                                "email", uniqueEmail,
                                "password", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value(uniqueEmail));
    }

    @Test
    void register_withDuplicateEmail_returns409() throws Exception {
        String email = "dup_" + System.currentTimeMillis() + "@example.com";
        createUserAndGetToken(email, "USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Dup User",
                                "email", email,
                                "password", "secret123"))))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withCorrectCredentials_returns200() throws Exception {
        String email = "login_" + System.currentTimeMillis() + "@example.com";
        createUserAndGetToken(email, "USER");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        String email = "badpwd_" + System.currentTimeMillis() + "@example.com";
        createUserAndGetToken(email, "USER");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "wrongpassword"))))
                .andExpect(status().isUnauthorized());
    }
}

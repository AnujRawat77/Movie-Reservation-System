package com.movie_reservation.MovieReservationSystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void getProfile_authenticated_returns200() throws Exception {
        String token = createUserAndGetToken("profile_user@test.com", "USER");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("profile_user@test.com"));
    }

    @Test
    void getProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_authenticated_returns200() throws Exception {
        String token = createUserAndGetToken("update_user@test.com", "USER");

        String body = """
                {"name": "Updated Name", "phone": "1234567890"}
                """;

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    void listAllUsers_asAdmin_returns200() throws Exception {
        String adminToken = createUserAndGetToken("admin_list@test.com", "ADMIN");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", bearerToken(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listAllUsers_asUser_returns403() throws Exception {
        String userToken = createUserAndGetToken("regular_list@test.com", "USER");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", bearerToken(userToken)))
                .andExpect(status().isForbidden());
    }
}

package com.movie_reservation.MovieReservationSystem.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void getMyReservations_authenticated_returns200() throws Exception {
        String token = createUserAndGetToken("res_user@test.com", "USER");

        mockMvc.perform(get("/api/reservations/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getMyReservations_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/reservations/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyReservations_withStatusFilter_returns200() throws Exception {
        String token = createUserAndGetToken("res_filter@test.com", "USER");

        mockMvc.perform(get("/api/reservations/me?status=CONFIRMED")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getMyReservations_withMovieTitleFilter_returns200() throws Exception {
        String token = createUserAndGetToken("res_filter2@test.com", "USER");

        mockMvc.perform(get("/api/reservations/me?movieTitle=Inception")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getAllReservations_asAdmin_returns200() throws Exception {
        String adminToken = createUserAndGetToken("res_admin@test.com", "ADMIN");

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", bearerToken(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getAllReservations_asUser_returns403() throws Exception {
        String userToken = createUserAndGetToken("res_user2@test.com", "USER");

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", bearerToken(userToken)))
                .andExpect(status().isForbidden());
    }
}

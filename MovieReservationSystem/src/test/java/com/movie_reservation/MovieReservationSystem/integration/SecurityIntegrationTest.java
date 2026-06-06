package com.movie_reservation.MovieReservationSystem.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SecurityIntegrationTest extends IntegrationTestBase {

    @Test
    void publicMoviesEndpoint_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk());
    }

    @Test
    void publicGenresEndpoint_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedReservationsMe_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/reservations/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminReports_asUser_returns403() throws Exception {
        String userToken = createUserAndGetToken("sec_user@test.com", "USER");

        mockMvc.perform(get("/api/reports/dashboard")
                        .header("Authorization", bearerToken(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminReports_asAdmin_returns200() throws Exception {
        String adminToken = createUserAndGetToken("sec_admin@test.com", "ADMIN");

        mockMvc.perform(get("/api/reports/dashboard")
                        .header("Authorization", bearerToken(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void protectedUserList_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}

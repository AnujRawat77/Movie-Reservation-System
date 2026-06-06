package com.movie_reservation.MovieReservationSystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie_reservation.MovieReservationSystem.entity.User;
import com.movie_reservation.MovieReservationSystem.repository.UserRepository;
import com.movie_reservation.MovieReservationSystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.web.FilterChainProxy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy filterChainProxy;

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(filterChainProxy)
                .build();
    }

    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected String createUserAndGetToken(String email, String role) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.builder()
                    .name("Test User")
                    .email(email)
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(role)
                    .build();
            return userRepository.save(u);
        });
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    protected String bearerToken(String token) {
        return "Bearer " + token;
    }
}

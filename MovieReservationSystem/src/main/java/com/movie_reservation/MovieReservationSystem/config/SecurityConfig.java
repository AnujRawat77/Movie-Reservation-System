package com.movie_reservation.MovieReservationSystem.config;

import com.movie_reservation.MovieReservationSystem.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h.frameOptions(f -> f.disable()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/genres/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/movies").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/genres").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/genres/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/showtimes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/showtimes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/showtimes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/*/bookings").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/halls").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/halls/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/halls/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/reservations").hasRole("ADMIN")
                // Seat-map requires auth (to resolve HELD_BY_ME); basic seats list is public
                .requestMatchers(HttpMethod.GET, "/api/showtimes/*/seat-map").authenticated()
                // Reviews: GET is public, POST/DELETE require auth
                .requestMatchers(HttpMethod.GET, "/api/movies/*/reviews").permitAll()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

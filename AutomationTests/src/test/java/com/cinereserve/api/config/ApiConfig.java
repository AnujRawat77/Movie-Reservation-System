package com.cinereserve.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * API Configuration - Base URL, timeouts, and environment settings.
 */
public class ApiConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream in = ApiConfig.class.getClassLoader()
                .getResourceAsStream("test.properties")) {
            if (in != null) properties.load(in);
        } catch (IOException e) {
            // use defaults
        }
    }

    public static final String BASE_URL =
            System.getProperty("api.base.url",
                    properties.getProperty("api.base.url", "http://localhost:8080"));

    public static final int CONNECT_TIMEOUT_MS =
            Integer.parseInt(properties.getProperty("connect.timeout.ms", "10000"));

    public static final int READ_TIMEOUT_MS =
            Integer.parseInt(properties.getProperty("read.timeout.ms", "30000"));

    // Default admin credentials
    public static final String ADMIN_EMAIL =
            properties.getProperty("admin.email", "admin@cinereserve.com");
    public static final String ADMIN_PASSWORD =
            properties.getProperty("admin.password", "Admin@123");

    // API Endpoints
    public static final class Endpoints {
        // Auth
        public static final String REGISTER         = "/api/auth/register";
        public static final String LOGIN            = "/api/auth/login";

        // Movies
        public static final String MOVIES           = "/api/movies";
        public static final String MOVIE_BY_ID      = "/api/movies/{id}";
        public static final String MOVIE_SHOWTIMES  = "/api/movies/{id}/showtimes";

        // Genres
        public static final String GENRES           = "/api/genres";
        public static final String GENRE_BY_ID      = "/api/genres/{id}";

        // Halls
        public static final String HALLS            = "/api/halls";
        public static final String HALL_BY_ID       = "/api/halls/{id}";

        // Showtimes
        public static final String SHOWTIMES        = "/api/showtimes";
        public static final String SHOWTIME_BY_ID   = "/api/showtimes/{id}";
        public static final String SHOWTIME_SEATS   = "/api/showtimes/{id}/seats";
        public static final String SHOWTIME_SEAT_MAP= "/api/showtimes/{id}/seat-map";

        // Reservations
        public static final String RESERVATIONS     = "/api/reservations";
        public static final String RESERVATION_ME   = "/api/reservations/me";
        public static final String RESERVATION_BY_ID= "/api/reservations/{id}";

        // Holds
        public static final String HOLDS            = "/api/holds";
        public static final String HOLD_BY_ID       = "/api/holds/{holdId}";
        public static final String HOLD_REFRESH     = "/api/holds/{holdId}/refresh";
        public static final String HOLD_CONFIRM     = "/api/holds/{holdId}/confirm";

        // Users
        public static final String USERS            = "/api/users";
        public static final String USER_ME          = "/api/users/me";
        public static final String USER_ROLE        = "/api/users/{id}/role";

        // Reports
        public static final String REPORT_REVENUE   = "/api/reports/revenue";
        public static final String REPORT_CAPACITY  = "/api/reports/capacity/{showtimeId}";
        public static final String REPORT_TOP_MOVIES= "/api/reports/top-movies";
    }
}

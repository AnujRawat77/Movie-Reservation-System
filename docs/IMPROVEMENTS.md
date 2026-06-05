# Movie Reservation System — Improvement Tracker

> Mark each checkbox as you implement it. Sections are ordered by priority.

---

## 1. Critical Fixes (Do These First)

### Security
- [ ] **Move JWT secret to environment variable** — `application.properties` has `jwt.secret` hardcoded in plain text; exposed in version control. Replace with `${JWT_SECRET}` env var.
- [ ] **Add rate limiting on auth endpoints** — `/api/auth/login` and `/api/auth/register` have no brute-force protection. Add `spring-security-ratelimit` or Bucket4j filter.
- [ ] **Implement refresh token flow** — Currently a single 24-hour JWT with no refresh. Add short-lived access token (15 min) + long-lived refresh token (7 days) with rotation.
- [ ] **Add server-side logout / token blacklist** — Logged-out tokens remain valid until expiry. Add a blacklist (Redis `SET` or DB table) and a `/api/auth/logout` endpoint.

### Database / Business Logic
- [ ] **Add UNIQUE constraint on `(seat_id, showtime_id)` in `reservation_seats`** — No SQL-level constraint exists to prevent double-booking. A race condition between two concurrent requests can book the same seat twice. Add `@UniqueConstraint` to `ReservationSeat` entity.
- [ ] **Specify `@Transactional` isolation level in `ReservationService`** — Currently uses default isolation. Set `Isolation.REPEATABLE_READ` (or `SERIALIZABLE`) on `createReservation()` to prevent phantom reads under concurrency.
- [ ] **Switch from H2 in-memory to file-based (dev) or PostgreSQL (prod)** — `jdbc:h2:mem:moviedb` loses all data on restart. Use `jdbc:h2:file:./data/moviedb` for dev and `postgresql` for production.
- [ ] **Change `ddl-auto` from `create-drop` to `update` or `validate`** — `create-drop` wipes the schema on every restart. Use `update` for dev, Flyway migrations for prod.

---

## 2. Bugs & Code Smells

### High Risk
- [x] **Fix N+1 query in `MovieService.getAllMovies()`** — Each movie triggers a separate query to load genres. Use `@Query("SELECT m FROM Movie m JOIN FETCH m.genres")` in the repository.
- [x] **Fix N+1 query in `ReportService.getTopMovies()`** — Revenue queried per-movie in a loop. Replace with a single aggregation query using `GROUP BY`.
- [x] **Fix filter logic pushed to Java streams in `MovieService`** — Status/genre filtering done in Java after fetching all movies. Push filters to JPQL `WHERE` clause via `@Query` or `Specification`.
- [x] **Fix filter logic in `ReportService.getRevenue()`** — Loads all reservations then filters in Java. Replace with a single SQL aggregation query.
- [x] **Add null-safety on `rs.getReservation()` in `ShowtimeService.getAvailableSeats()`** — `rs.getReservation()` could be `null` if orphaned `ReservationSeat` records exist, causing NPE in the stream filter.

### Medium Risk (Code Smells)
- [x] **Replace hardcoded role strings with an enum or constants** — `"ROLE_ADMIN"`, `"ROLE_USER"`, `"USER"`, `"ADMIN"` appear as raw strings across `SecurityConfig`, `UserDetailsServiceImpl`, `ReservationController`. Define a `Role` enum.
- [x] **Replace hardcoded status strings with enums** — `"CONFIRMED"`, `"CANCELLED"`, `"SCHEDULED"`, `"PREMIUM"`, `"REGULAR"` scattered across services. Define `ReservationStatus`, `ShowtimeStatus`, `SeatType` enums.
- [x] **Extract hardcoded premium seat multiplier `1.5`** — `ReservationService` multiplies price by `new BigDecimal("1.5")` directly. Move to `application.properties` as `reservation.premium-seat-multiplier`.
- [x] **Extract hardcoded 15-minute showtime buffer** — `ShowtimeService` uses `.plusMinutes(15)` in two places. Move to `application.properties` as `showtime.buffer-minutes`.
- [x] **Fix hardcoded row label array in `HallService`** — `String[] rowLabels = {"A","B",...,"T"}` limits halls to 20 rows. Generate dynamically from alphabet.
- [x] **Fix hardcoded premium row designations** — `row.equals("D") || row.equals("E")` in `HallService` and `DataSeeder`. Make configurable per hall or per seat type.
- [x] **Add `@Slf4j` logging to all services** — Zero logging exists in any service class. Impossible to debug production issues without it.
- [x] **Add `@URL` validation on `MovieRequest.posterUrl`** — No format validation on the poster URL field.
- [x] **Add regex/enum validation on `UserRoleRequest.role`** — Field has `@NotBlank` but accepts any string. Add `@Pattern(regexp = "^(USER|ADMIN)$")`.
- [x] **Fix inconsistent error response format** — `GlobalExceptionHandler` returns `Map<String, Object>` while controllers use `ApiResponse<T>`. Unify to one format.
- [x] **Return array of field errors from `GlobalExceptionHandler`** — Validation errors are joined into a single comma-separated string, making them hard to parse on the frontend.
- [x] **Add timestamp to error responses** — No `timestamp` field in error payloads, making log correlation hard.
- [x] **Log stack trace in generic exception handler** — The catch-all `Exception` handler in `GlobalExceptionHandler` doesn't log the stack trace.

---

## 3. Performance Optimizations

- [ ] **Add pagination to all list endpoints** — `/api/movies`, `/api/showtimes`, `/api/users`, `/api/reservations` return entire datasets. Add `Pageable` parameter and return `Page<T>`.
- [ ] **Add database indexes** — No explicit indexes defined. Add indexes on: `users.email`, `reservations.user_id`, `reservations.showtime_id`, `reservation_seats.seat_id`, `reservation_seats.showtime_id`, `showtimes.movie_id`, `showtimes.hall_id`, `movies.is_deleted`.
- [ ] **Add caching for static/slow-changing data** — Genres, halls, and movie listings are read far more than written. Add `@Cacheable` with Caffeine (in-memory) or Redis.
- [ ] **Enable response GZip compression** — Add `server.compression.enabled=true` to `application.properties`. Zero-code win for large JSON responses.
- [ ] **Configure HikariCP connection pool explicitly** — Currently using defaults. Set `spring.datasource.hikari.maximum-pool-size` based on expected load.
- [ ] **Add `@EntityGraph` for specific queries** — Avoid repeated lazy-load round trips on reservation/showtime detail fetches.
- [ ] **Enable Hibernate batch inserts** — Add `spring.jpa.properties.hibernate.jdbc.batch_size=20` and `order_inserts=true` for bulk seat creation in `HallService`.

---

## 4. Infrastructure & Configuration

- [ ] **Add Flyway for database migrations** — No schema versioning tool. Add `flyway-core` dependency; migrate away from `ddl-auto`. Required before any production deployment.
- [ ] **Add Spring Boot Actuator** — No health checks, metrics, or readiness probes. Add `spring-boot-starter-actuator` for `/actuator/health`, `/actuator/metrics`.
- [ ] **Add Micrometer + Prometheus metrics** — No observability. Add `micrometer-registry-prometheus` to expose metrics for Grafana dashboards.
- [ ] **Create `application-dev.properties` and `application-prod.properties`** — Single properties file mixes dev (H2, console enabled) and prod concerns. Split into Spring profiles.
- [ ] **Configure structured JSON logging** — Add `logback-spring.xml` with JSON appender (e.g., `logstash-logback-encoder`) so logs are parseable by log aggregators.
- [ ] **Add `@EnableAsync` and configure thread pool** — No async processing configured. Needed for email sending, report generation without blocking HTTP threads.

---

## 5. Security Hardening (Non-Critical but Important)

- [ ] **Add email verification flow** — Accounts can be created with fake emails. Implement: register → send token via email → `/api/auth/verify-email?token=...` → activate account.
- [ ] **Strengthen password policy** — Only a 6-character minimum enforced. Add: max length, at least one uppercase, one digit, one special character. Consider common-password blacklist.
- [ ] **Add audit logging for sensitive operations** — No record of who changed roles, who cancelled reservations, etc. Log to a separate `audit_log` table or dedicated logger.
- [ ] **Add HTTPS configuration** — No TLS config. Add `server.ssl.*` properties or configure behind a reverse proxy (NGINX/Traefik) with HTTPS termination.
- [ ] **Move token from `localStorage` to `HttpOnly` cookie** — Frontend stores JWT in `localStorage`, vulnerable to XSS. Migrate to `HttpOnly; Secure; SameSite=Strict` cookie.
- [ ] **Add cross-field validation to `ShowtimeRequest`** — No check that `endTime > startTime`. Add a custom `@ValidShowtime` constraint annotation.
- [ ] **Add max bounds to `HallRequest`** — No upper limit on `totalRows` or `seatsPerRow`. A user could create a hall with 10,000 rows and crash seat generation.

---

## 6. New Features to Add

### Core Booking Improvements
- [x] **Seat hold / temporary reservation** — Reserve seats for 10–15 minutes while user completes payment. Auto-release on timeout. Needs a background scheduler (`@Scheduled`) or Redis TTL.
- [ ] **Reservation modification** — Allow users to change seats or showtime on an existing booking (with business rules on cutoff time).
- [ ] **Waitlist system** — When a showtime is full, allow users to join a queue. Auto-notify (email/in-app) and convert to booking when a seat is released.
- [x] **Cancellation policy & refund tracking** — Define policy (full refund > 24h, 50% < 24h, no refund < 2h). Track `refundAmount`, `refundStatus`, `cancellationReason` on `Reservation`.

### Payment
- [ ] **Payment integration (Stripe / Razorpay)** — Currently no payment processing. Add a payment service with webhook handling for confirmation and refunds.
- [ ] **Invoice / receipt PDF generation** — Generate a downloadable PDF receipt per booking using iText or JasperReports.
- [ ] **Discount codes / coupons** — Add a `Coupon` entity with code, discount type (fixed/percent), expiry, and per-user usage limits.

### Notifications
- [ ] **Email confirmation on booking** — Send booking confirmation email with seat details via Spring Mail (`spring-boot-starter-mail` + async `@EventListener`).
- [ ] **Email on cancellation** — Send cancellation and refund status email.
- [ ] **Password reset via email** — `/api/auth/forgot-password` → email with time-limited token → `/api/auth/reset-password`.
- [ ] **Upcoming showtime reminder** — Email reminder 2 hours before showtime using `@Scheduled`.

### Movie & Content
- [x] **Movie ratings & reviews** — Add `Review` entity (user, movie, rating 1–5, comment, timestamp). Expose GET/POST `/api/movies/{id}/reviews`.
- [x] **Movie search & advanced filters** — Full-text search on title/description, filter by genre, rating, language, release date range.
- [x] **Trailer / media links** — Add `trailerUrl`, `language`, `censorRating` (G/PG/PG-13/R), `director`, `cast` fields to `Movie`.
- [ ] **Upcoming movies / pre-booking** — Allow creating movies with `status=SOON` and let users register interest before showtimes are created.

### Admin & Analytics
- [ ] **Admin dashboard API** — Endpoints for daily/weekly/monthly sales, occupancy rate trends, top-performing movies/halls, revenue by genre.
- [ ] **Bulk showtime creation** — Create recurring showtimes (e.g., every day at 6pm for 2 weeks) in one API call.
- [ ] **User management** — Admin: suspend/activate users (`isActive` flag), view booking history per user.
- [ ] **Export reports** — Export revenue/occupancy reports as CSV or Excel (`Apache POI`).

### User Experience
- [ ] **User profile** — Allow users to update name, phone, profile picture. Add `phone`, `address`, `profilePictureUrl` to `User`.
- [ ] **Booking history with filters** — Filter `GET /api/reservations/me` by status, date range, movie title.
- [ ] **Loyalty points system** — Award points per booking. Allow redemption for discounts. Needs `loyaltyPoints` on `User` and transaction log.
- [ ] **Favorite movies / watchlist** — Users can bookmark movies. `GET /api/users/me/watchlist`, `POST/DELETE /api/movies/{id}/watchlist`.

### Hall & Seat Management
- [ ] **Hall screen type** — Add `screenType` (2D / 3D / IMAX) and `soundSystem` (Dolby / THX) to `Hall`.
- [ ] **Accessibility seat flags** — Add `handicapAccessible`, `wheelchairSpace` booleans to `Seat`.
- [x] **Seat map visual API** — Endpoint that returns seat layout as a structured grid (row → seats) for the frontend seat picker.

---

## 7. Testing

- [ ] **Unit tests for `ReservationService`** — Core business logic (pricing, seat validation, double-booking, status transitions) has zero test coverage.
- [ ] **Unit tests for `AuthService`** — Test registration, login, duplicate email, wrong password paths.
- [ ] **Unit tests for `ShowtimeService`** — Overlap detection logic is complex and untested.
- [ ] **Unit tests for `HallService`** — Seat generation logic (row labels, premium designation) is untested.
- [ ] **Integration tests for all controllers** — Use `@SpringBootTest` + `MockMvc` to test endpoint authorization, validation, and happy paths.
- [x] **Concurrency test for double-booking** — Write a test that fires 2 concurrent reservation requests for the same seat and asserts only one succeeds.
- [ ] **Repository query tests** — Test custom JPQL queries with `@DataJpaTest`.
- [ ] **Security integration tests** — Test that unauthenticated / wrong-role requests return 401 / 403.

---

## 8. Developer Experience & Documentation

- [ ] **Add OpenAPI examples to all endpoints** — Swagger docs exist but lack request/response examples. Add `@Operation` and `@ApiResponse` annotations with `example` values.
- [ ] **Add `docker-compose.yml`** — Local setup requires manual steps. A compose file with PostgreSQL (or H2), the app, and Redis would let anyone run the system with one command.
- [ ] **Add `Dockerfile`** — No containerization. Add a multi-stage `Dockerfile` (`builder` stage for Maven build, `runtime` stage with JRE 21).
- [ ] **Add `.env.example`** — Document all required environment variables (JWT_SECRET, DB credentials, mail config) so new developers know what to set.

---

## 9. UI Test Coverage Gaps (~67% covered)

> Core booking journey has zero UI test coverage. See `AutomationTests/` for existing tests.

- [ ] **Booking creation flow** (`/booking/:showtimeId`) — seat selection, seat availability display, and submitting a reservation are entirely untested. Most critical user journey.
- [ ] **Booking success / confirmation page** (`/booking/success/:id`) — no test verifies the post-booking confirmation screen.
- [ ] **Actual signup flow** — existing test only checks if form fields exist; never submits the form and verifies a successful registration.
- [ ] **User profile page** (`/profile`) — no tests at all.
- [ ] **Admin genres page** (`/admin/genres`) — not covered in `admin.feature`.
- [ ] **Admin halls page** (`/admin/halls`) — not covered in `admin.feature`.
- [ ] **Admin showtimes page** (`/admin/showtimes`) — not covered in `admin.feature`.
- [ ] **Admin reservations page** (`/admin/reservations`) — not covered in `admin.feature`.
- [ ] **Movie detail page interactions** (`/movies/:id`) — page object exists but no feature scenario covers viewing showtimes, selecting a showtime, or navigating to booking.

---

*Last updated: 2026-06-05*

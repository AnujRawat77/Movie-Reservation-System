# CHANGELOG

## Version 1.1.0 - June 6, 2026
### New Features
- **2-Phase Seat Hold System** — Temporary seat reservation with 5-minute TTL; `UNIQUE(showtime_id, seat_id)` DB constraint prevents double-booking under concurrent load; `@Scheduled` 60s background cleanup auto-expires stale holds
- **Seat Hold API** — Full hold lifecycle: create, get, refresh, release, confirm (4 new endpoints under `/api/holds`)
- **Loyalty Points Engine** — Awards 10 pts/$1 on confirmed bookings; transactional redemption with full audit log (`loyalty_transactions` table)
- **Watchlist / Favorites** — Bookmark movies with `UNIQUE(user_id, movie_id)` constraint; toggle button on movie detail page
- **User Profile Management** — `PUT /api/users/me` for updating name, phone, and address
- **Booking History Filters** — `GET /api/reservations/me` now accepts `status`, `from`, `to`, `movie` query params via custom JPQL
- **Payment Confirmation Flow** — Frontend `/booking/confirm/:holdId` route with countdown timer and mock payment form

### Testing
- **3-Layer Automated Test Suite** — 100+ test cases total
  - RestAssured API tests across 11 modules (auth, movies, halls, showtimes, holds, reservations, reports, security, genres, users, integration)
  - JUnit 5 + Mockito unit tests for 4 core services (`AuthService`, `ReservationService`, `ShowtimeService`, `HallService`)
  - `@SpringBootTest` integration tests: Auth/User/Reservation controllers, security layer, repository queries
  - Selenium + Cucumber BDD UI tests across 12 feature files with Allure HTML reporting
  - `CountDownLatch` concurrency test confirming exactly 1 of 2 simultaneous hold requests succeeds

### New Entities
- `SeatHold` — seat_holds table (ACTIVE/CONFIRMED/EXPIRED/RELEASED lifecycle)
- `SeatAllocation` — seat_allocations table (per-seat concurrency lock)
- `LoyaltyTransaction` — loyalty_transactions table (EARNED/REDEEMED audit)
- `Watchlist` — watchlist table

### New Controllers / Services
- `LoyaltyController`, `LoyaltyService`
- `WatchlistController`, `WatchlistService`
- `SeatHoldController`, `SeatHoldService` (full implementation)

### Project Statistics (updated)
- 13 Controllers, 13 Services, 13 Repositories
- 12 JPA Entities
- 45+ REST API endpoints
- 13 database tables
- ~3,800+ lines of Java code
- ~4,500+ lines of TypeScript code
- 100+ automated test cases

---

## Version 1.0.0 - May 31, 2026
### Features Implemented
- JWT-based authentication with BCrypt password hashing
- Role-based access control (ADMIN/USER)
- Movie CRUD with multi-genre support
- Multi-seat atomic reservations with concurrency control
- Showtime scheduling with overlap prevention
- Revenue and occupancy reporting
- Admin dashboard with full management capabilities
- Modern React UI with Tailwind CSS
- API documentation with Swagger/OpenAPI
- H2 database with automatic schema creation and data seeding
### Tech Stack
**Backend**: Spring Boot 4.0.6, Java 21, JPA/Hibernate, JWT, Spring Security
**Frontend**: React 19.2.0, TypeScript, TanStack Router, Tailwind CSS
**Database**: H2 (Development)
### Project Statistics
- 8 Controllers, 8 Services, 8 Repositories
- 8 JPA Entities with complex relationships
- 30+ REST API endpoints
- 9 database tables
- 2500+ lines of Java code
- 3000+ lines of TypeScript code
### Known Limitations
- H2 in-memory database (resets on restart)
- No payment processing
- No email notifications
- No WebSocket real-time updates
### Future Enhancements
- Real payment gateway integration
- Email notifications for booking confirmation
- OAuth / social login
- WebSocket real-time seat updates (replace polling)
- QR code tickets
- Multi-location / multi-cinema support
- PostgreSQL for production
- Redis caching for seat-map reads
- Containerisation (`Dockerfile` + `docker-compose.yml`)

**Status**: ✅ Complete and Functional — v1.1.0

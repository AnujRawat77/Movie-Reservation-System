# System Architecture & Design
## Layered Architecture Overview
The Movie Reservation System follows a **Three-Tier Layered Architecture**:
\\\
Frontend (React)
    ↓ REST API with JWT
Backend (Spring Boot)
    ├─ Controllers (HTTP layer)
    ├─ Services (Business logic)
    └─ Repositories (Data access)
    ↓ JPA/Hibernate ORM
Database (H2/PostgreSQL)
\\\
---
## Key Architecture Decisions
### 1. Concurrency Control Strategy (3-Layer Defense)
- **Layer 1 — Pre-flight check**: `ReservationSeatRepository.existsBySeatIdAndShowtimeIdAndReservationStatus()` fast-rejects already-confirmed seats before issuing a hold
- **Layer 2 — DB Unique Constraint**: `UNIQUE(showtime_id, seat_id)` on `seat_allocations`; only one concurrent INSERT wins; losing request receives `DataIntegrityViolationException` → HTTP 409 (`SEAT_ALREADY_HELD`)
- **Layer 3 — Transaction scope**: entire `SeatHoldService.createHold()` is `@Transactional`; any exception rolls back all allocations — no orphaned rows
- **Background Cleanup**: `@Scheduled(fixedDelay=60_000)` purges expired `SeatAllocation` and `SeatHold` rows every 60 seconds
### 2. 2-Phase Seat Hold System
- **Hold creation**: reserves `SeatAllocation` rows with a 5-minute TTL, returns `holdId`
- **Frontend countdown**: React UI shows live countdown; user completes payment form before expiry
- **Confirmation**: `POST /api/holds/{holdId}/confirm` atomically converts the hold into a `Reservation + ReservationSeats` while deleting allocations
- **TTL refresh**: `POST /api/holds/{holdId}/refresh` extends expiry by 5 minutes if the user is still on the payment page
### 3. Loyalty Points Engine
- Awards **10 points per $1** on every confirmed booking (`SeatHoldService.confirmHold()` → `LoyaltyService.awardPoints()`)
- Redemption is transactional: deduct from `User.loyaltyPoints` and write a `LoyaltyTransaction(Type.REDEEMED)` atomically
- Full audit trail: every earn/redeem event is stored in `loyalty_transactions` with timestamp and linked `reservationId`
### 4. Authentication & Security
- **JWT Tokens**: Stateless authentication for scalability
- **Password Hashing**: BCrypt with cost factor 12 (2^12 iterations)
- **Role-Based Access**: Spring Security with `@PreAuthorize` annotations
- **CORS Protection**: Configured for development environment
### 5. Data Model Design
- **Many-to-Many**: Movies ↔ Genres via junction table; Users ↔ Movies (watchlist)
- **Soft Deletes**: Movies marked `isDeleted` instead of hard delete
- **UUID Primary Keys**: For reservations (distributed-friendly)
- **Cascade Operations**: ReservationSeats cascade with Reservation
### 6. Booking Workflow (Updated)
```
1. POST /api/holds  — validate seats available, insert seat_allocations (UNIQUE guard), return holdId + 5-min TTL
2. Frontend countdown — user fills payment form within TTL
3. POST /api/holds/{holdId}/confirm — atomically:
   a. Verify hold not expired
   b. Create Reservation + ReservationSeats
   c. Delete SeatAllocation rows (release hold)
   d. Award loyalty points
4. On timeout / user cancels — DELETE /api/holds/{holdId} or scheduler auto-cleans
```
### 7. API Design Principles
- **RESTful**: Resource-based endpoints
- **Consistent Responses**: Standardised JSON format
- **Error Codes**: Business-specific error codes (`SEAT_ALREADY_HELD`, `HOLD_EXPIRED`, etc.)
- **Versioning**: Ready for `/v2` API endpoints
### 8. Performance Optimisations
- **Lazy Loading**: M:N relationships use LAZY fetch
- **Database Indexes**: Auto-indexed foreign keys
- **Minimal Transactions**: Keep transaction scope as small as possible
- **Connection Pooling**: Via HikariCP (default with Spring Boot)
---
## Frontend Architecture
### Component Structure
\\\
src/
├─ routes/           # Page components
│  ├─ auth pages
│  ├─ movie browsing
│  ├─ booking flow
│  └─ admin dashboard
├─ components/       # Reusable components
├─ hooks/            # Custom React hooks
├─ lib/              # API client, utilities
├─ constants/        # App-wide constants
└─ styles/           # Global styles
\\\
### State Management
- **Server State**: TanStack React Query for API data
- **Local State**: React hooks for UI state
- **Auth State**: Custom useAuth hook with localStorage
- **Sync**: Event listeners for cross-tab auth changes
---
## Database Relationships
```
User (1) ──→ (N) Reservation
User (1) ──→ (N) SeatHold
User (1) ──→ (N) LoyaltyTransaction
User (1) ──→ (N) Watchlist ──→ (N) Movie
          ↓
      Showtime (1) ──→ (N) ReservationSeat
      Showtime (1) ──→ (N) SeatAllocation
          ↓                        ↓
      Movie (1) ──→ (N) Showtime  Seat
          ↓
      Genre (M:N via MovieGenre)
      Hall (1) ──→ (N) Seat
           ↓
      Showtime
`
---
## Security Layers
1. **CORS Filter**: Validates request origin
2. **JWT Filter**: Extracts and validates token 
3. **Spring Security Filter**: Checks user roles and permissions
4. **Controller**: Request validation
5. **Service**: Business rule enforcement
6. **Database**: Constraints (PK, FK, UNIQUE)
---
## Error Handling Strategy
**Custom Exceptions**:
- BusinessException (400/409) - Rule violations
- ResourceNotFoundException (404) - Resource not found
- UnauthorizedException (401/403) - Auth issues
**Global Handler**: StandardResponseEntityAdviceHandler catches all and returns consistent format
---
## Database Query Optimization
**Indexed Fields**:
- user.email (UNIQUE + indexed)
- showtime.movie_id (FK indexed)
- showtime.hall_id (FK indexed)
- reservation.user_id (FK indexed)
- reservation_seats.seat_id (FK indexed)
- seat_allocations.showtime_id + seat_id (UNIQUE)
- loyalty_transactions.user_id (FK indexed)
- watchlist.user_id + movie_id (UNIQUE)

---

**Version**: 1.1.0 | **Last Updated**: June 6, 2026

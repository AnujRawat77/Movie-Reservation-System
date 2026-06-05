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
### 1. Concurrency Control Strategy
- **Database Unique Constraint**: UNIQUE(showtime_id, seat_id) on ReservationSeat table
- **Transactional Operations**: @Transactional at service method level  
- **Pre-booking Validation**: Verify each seat availability before creating reservation
- **Automatic Rollback**: On any validation failure, entire transaction rolls back
### 2. Authentication & Security
- **JWT Tokens**: Stateless authentication for scalability
- **Password Hashing**: BCrypt with cost factor 12 (2^12 iterations)
- **Role-Based Access**: Spring Security with @PreAuthorize annotations
- **CORS Protection**: Configured for development environment
### 3. Data Model Design
- **Many-to-Many**: Movies ↔ Genres via junction table
- **Soft Deletes**: Movies marked isDeleted instead of hard delete
- **UUID Primary Keys**: For reservations (distributed-friendly)
- **Cascade Operations**: ReservationSeats cascade with Reservation
### 4. Booking Workflow
`
1. Validate showtime exists & is future
2. For each seat: check not already booked
3. Calculate total (premium seats = 1.5x)
4. Create Reservation + ReservationSeats
5. All-or-nothing: commit or rollback
`
### 5. API Design Principles
- **RESTful**: Resource-based endpoints
- **Consistent Responses**: Standardized JSON format
- **Error Codes**: Business-specific error codes (SEAT_ALREADY_BOOKED, etc.)
- **Versioning**: Ready for /v2 API endpoints
### 6. Performance Optimizations
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
`
User (1) ──→ (N) Reservation
          ↓
      Showtime (1) ──→ (N) ReservationSeat
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
---
**Version**: 1.0.0 | **Last Updated**: May 31, 2026

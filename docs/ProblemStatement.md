# Movie Reservation System — Detailed Problem Description

## 📋 Table of Contents

1. [Project Overview](#1-project-overview)
2. [Objectives & Learning Goals](#2-objectives--learning-goals)
3. [System Scope](#3-system-scope)
4. [Functional Requirements](#4-functional-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [User Roles & Permissions](#6-user-roles--permissions)
7. [Data Model & Entity Relationships](#7-data-model--entity-relationships)
8. [API Endpoints Specification](#8-api-endpoints-specification)
9. [Business Rules & Logic](#9-business-rules--logic)
10. [Concurrency & Overbooking Prevention](#10-concurrency--overbooking-prevention)
11. [Authentication & Authorization](#11-authentication--authorization)
12. [Reporting Module](#12-reporting-module)
13. [Error Handling & Validation](#13-error-handling--validation)
14. [Tech Stack Recommendations](#14-tech-stack-recommendations)
15. [Project Structure](#15-project-structure)
16. [Seed Data](#16-seed-data)
17. [Testing Strategy](#17-testing-strategy)
18. [Deliverables](#18-deliverables)
19. [Optional Extensions](#19-optional-extensions)

---

## 1. Project Overview

### 1.1 Description
The **Movie Reservation System** is a backend service that simulates the operations of a movie ticket booking platform (similar to BookMyShow, AMC, or Fandango). It enables:

- **End users** to register, browse movies, view showtimes, select and reserve seats, and manage their bookings.
- **Administrators** to manage movies, showtimes, monitor reservations, and view revenue/occupancy reports.

### 1.2 Problem Statement
Build a robust, secure, and scalable backend system that handles:
- Multi-role user authentication (Admin / Regular User)
- Movie & showtime catalog management
- Concurrent seat reservation without overbooking
- Reservation lifecycle (create, view, cancel)
- Administrative reporting (revenue, occupancy, sales)

---

## 2. Objectives & Learning Goals

By completing this project, you should master:

- ✅ Designing **normalized relational schemas** with complex relationships
- ✅ Implementing **role-based access control (RBAC)**
- ✅ Handling **concurrency conflicts** (race conditions on seat booking)
- ✅ Writing **complex SQL queries** for reporting & analytics
- ✅ Building **RESTful APIs** with proper HTTP semantics
- ✅ Applying **transactional integrity** (ACID compliance)
- ✅ Implementing **JWT-based authentication**
- ✅ Writing **business logic** that enforces real-world constraints

---

## 3. System Scope

### 3.1 In Scope
- User registration, login, JWT-based session management
- Admin & user roles, role promotion (admin only)
- CRUD operations on movies, genres, theaters, showtimes
- Seat layout management per theater/hall
- Seat reservation with conflict resolution
- Reservation cancellation (only future/upcoming reservations)
- Revenue and capacity reports
- Filtering movies by date, genre, title

### 3.2 Out of Scope (Optional Extensions)
- Payment gateway integration
- Email/SMS notifications
- Recommendation engine
- Reviews/ratings system
- Multi-language support
- Real-time websocket seat updates

---

## 4. Functional Requirements

### 4.1 User Authentication
| ID | Requirement |
|----|-------------|
| FR-1.1 | Users can register with name, email, and password |
| FR-1.2 | Users can log in and receive a JWT token |
| FR-1.3 | Passwords must be securely hashed (bcrypt/argon2) |
| FR-1.4 | Tokens must expire and support refresh (optional) |
| FR-1.5 | Logout invalidates tokens (optional with token blacklist) |

### 4.2 User Management
| ID | Requirement |
|----|-------------|
| FR-2.1 | Admins can promote regular users to admin role |
| FR-2.2 | Admins can demote other admins |
| FR-2.3 | Users can view & update their own profile |
| FR-2.4 | Admins can list all users |

### 4.3 Movie Management (Admin Only)
| ID | Requirement |
|----|-------------|
| FR-3.1 | Admins can create a movie with title, description, poster image URL, duration, genre(s) |
| FR-3.2 | Admins can update movie details |
| FR-3.3 | Admins can delete a movie (soft delete recommended if it has reservations) |
| FR-3.4 | Movies can belong to multiple genres |
| FR-3.5 | Admins can manage genres (CRUD) |

### 4.4 Showtime Management (Admin Only)
| ID | Requirement |
|----|-------------|
| FR-4.1 | Admins can schedule showtimes for a movie at a specific theater/hall |
| FR-4.2 | A showtime has start time, end time (auto-calculated from movie duration), price |
| FR-4.3 | Showtimes for the same hall must not overlap |
| FR-4.4 | Admins can update or cancel showtimes |
| FR-4.5 | Cancelling a showtime should handle existing reservations (refund/notify) |

### 4.5 Browsing (All Users)
| ID | Requirement |
|----|-------------|
| FR-5.1 | Users can list movies, optionally filtered by genre or date |
| FR-5.2 | Users can view showtimes for a movie on a given date |
| FR-5.3 | Users can view available seats for a specific showtime |

### 4.6 Reservation Management
| ID | Requirement |
|----|-------------|
| FR-6.1 | Logged-in users can reserve one or more seats for a showtime |
| FR-6.2 | Already booked seats cannot be reserved again |
| FR-6.3 | Users can list their reservations (upcoming and past) |
| FR-6.4 | Users can cancel only **upcoming** reservations (before showtime begins) |
| FR-6.5 | A reservation must be atomic — all seats reserved or none |

### 4.7 Reporting (Admin Only)
| ID | Requirement |
|----|-------------|
| FR-7.1 | Admins can view all reservations with filters (date, movie, user) |
| FR-7.2 | Admins can view occupancy/capacity per showtime |
| FR-7.3 | Admins can view total revenue (overall, per movie, per date range) |
| FR-7.4 | Admins can view top-grossing movies |

---

## 5. Non-Functional Requirements

| Category | Requirement |
|----------|-------------|
| **Security** | Passwords hashed; JWT signed; SQL injection-proof (use ORM/parameterized queries) |
| **Performance** | Seat availability lookups should be O(1)/O(n) per showtime; use indexes |
| **Concurrency** | Booking same seat by multiple users must not result in double-booking |
| **Scalability** | Schema should support millions of reservations |
| **Reliability** | Database transactions for booking & cancellation |
| **Maintainability** | Clean code, modular architecture (controllers/services/repositories) |
| **Documentation** | API documentation via Swagger/OpenAPI or Postman collection |

---

## 6. User Roles & Permissions

### 6.1 Role Matrix

| Action | Guest | Regular User | Admin |
|--------|:-----:|:------------:|:-----:|
| Register / Login | ✅ | ✅ | ✅ |
| Browse movies & showtimes | ✅ | ✅ | ✅ |
| View available seats | ❌ | ✅ | ✅ |
| Reserve a seat | ❌ | ✅ | ✅ |
| View own reservations | ❌ | ✅ | ✅ |
| Cancel own (upcoming) reservation | ❌ | ✅ | ✅ |
| Manage movies (CRUD) | ❌ | ❌ | ✅ |
| Manage showtimes (CRUD) | ❌ | ❌ | ✅ |
| Promote/demote users | ❌ | ❌ | ✅ |
| View all reservations | ❌ | ❌ | ✅ |
| View revenue & reports | ❌ | ❌ | ✅ |

---

## 7. Data Model & Entity Relationships

### 7.1 Core Entities

#### **User**
| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID/INT | PK |
| name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| role | ENUM('USER','ADMIN') | DEFAULT 'USER' |
| created_at | TIMESTAMP | DEFAULT now() |
| updated_at | TIMESTAMP | |

#### **Genre**
| Field | Type | Constraints |
|-------|------|-------------|
| id | INT | PK |
| name | VARCHAR(50) | UNIQUE, NOT NULL |

#### **Movie**
| Field | Type | Constraints |
|-------|------|-------------|
| id | INT | PK |
| title | VARCHAR(255) | NOT NULL |
| description | TEXT | |
| poster_url | VARCHAR(500) | |
| duration_minutes | INT | NOT NULL, > 0 |
| is_deleted | BOOLEAN | DEFAULT false |
| created_at | TIMESTAMP | |

#### **MovieGenre** *(Junction Table — M:N)*
| Field | Type |
|-------|------|
| movie_id | FK → Movie |
| genre_id | FK → Genre |
| PRIMARY KEY (movie_id, genre_id) | |

#### **Theater / Hall**
| Field | Type | Constraints |
|-------|------|-------------|
| id | INT | PK |
| name | VARCHAR(100) | |
| total_rows | INT | |
| seats_per_row | INT | |

#### **Seat**
| Field | Type | Constraints |
|-------|------|-------------|
| id | INT | PK |
| hall_id | FK → Hall | |
| row_label | CHAR(2) | e.g. 'A', 'B' |
| seat_number | INT | |
| seat_type | ENUM('REGULAR','PREMIUM') | |
| UNIQUE(hall_id, row_label, seat_number) | | |

#### **Showtime**
| Field | Type | Constraints |
|-------|------|-------------|
| id | INT | PK |
| movie_id | FK → Movie | |
| hall_id | FK → Hall | |
| start_time | TIMESTAMP | NOT NULL |
| end_time | TIMESTAMP | NOT NULL |
| price | DECIMAL(10,2) | NOT NULL |
| status | ENUM('SCHEDULED','CANCELLED') | |

> 🔒 **Constraint:** No overlapping showtimes on the same hall.

#### **Reservation**
| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID | PK |
| user_id | FK → User | |
| showtime_id | FK → Showtime | |
| status | ENUM('CONFIRMED','CANCELLED') | |
| total_amount | DECIMAL(10,2) | |
| created_at | TIMESTAMP | |

#### **ReservationSeat**
| Field | Type | Constraints |
|-------|------|-------------|
| id | INT | PK |
| reservation_id | FK → Reservation | |
| seat_id | FK → Seat | |
| **UNIQUE(showtime_id, seat_id) WHERE status='CONFIRMED'** | | (Critical: prevents double booking) |

### 7.2 ER Diagram (Conceptual)

```
User ──< Reservation >── Showtime ──> Movie ──< MovieGenre >── Genre
                              │              
                              └──> Hall ──< Seat
                                              ▲
                                              │
                              Reservation ──< ReservationSeat
```

---

## 8. API Endpoints Specification

### 8.1 Authentication
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | Register user | Public |
| POST | `/api/auth/login` | Login & get JWT | Public |
| POST | `/api/auth/refresh` | Refresh token | Authenticated |

### 8.2 Users
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/users/me` | Get own profile | Authenticated |
| GET | `/api/users` | List all users | Admin |
| PATCH | `/api/users/:id/role` | Promote/demote | Admin |

### 8.3 Movies
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/movies` | List movies (filter: genre, date) | Public |
| GET | `/api/movies/:id` | Get movie details | Public |
| POST | `/api/movies` | Create movie | Admin |
| PUT | `/api/movies/:id` | Update movie | Admin |
| DELETE | `/api/movies/:id` | Delete movie | Admin |

### 8.4 Genres
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/genres` | List genres | Public |
| POST | `/api/genres` | Create genre | Admin |
| DELETE | `/api/genres/:id` | Delete genre | Admin |

### 8.5 Showtimes
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/movies/:id/showtimes?date=YYYY-MM-DD` | List showtimes | Public |
| GET | `/api/showtimes/:id/seats` | Available seats | Authenticated |
| POST | `/api/showtimes` | Create showtime | Admin |
| PUT | `/api/showtimes/:id` | Update showtime | Admin |
| DELETE | `/api/showtimes/:id` | Cancel showtime | Admin |

### 8.6 Reservations
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/reservations` | Create reservation `{showtime_id, seat_ids[]}` | User |
| GET | `/api/reservations/me` | List own reservations | User |
| DELETE | `/api/reservations/:id` | Cancel own reservation | User |
| GET | `/api/reservations` | List all (admin) | Admin |

### 8.7 Reports
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/reports/revenue?from=&to=` | Total revenue | Admin |
| GET | `/api/reports/capacity/:showtime_id` | Showtime occupancy | Admin |
| GET | `/api/reports/top-movies` | Top-grossing movies | Admin |

---

## 9. Business Rules & Logic

### 9.1 Movie Rules
- A movie cannot be hard-deleted if it has active future showtimes (use soft delete).
- Movie duration determines showtime end time automatically.

### 9.2 Showtime Rules
- `start_time` must be in the future (when creating).
- `end_time = start_time + movie.duration_minutes + buffer (e.g., 15 min)`.
- Two showtimes in the same hall **must not overlap**.

### 9.3 Reservation Rules
- A user cannot reserve a seat already reserved for the same showtime.
- A user can reserve **multiple** seats in one transaction.
- A reservation can only be **cancelled before** the showtime starts.
- Cancellation marks the reservation as `CANCELLED` (don't hard-delete; needed for reports).
- Seats from cancelled reservations become available again.

### 9.4 Pricing
- Base price set per showtime.
- Optionally, premium seats may have a multiplier (e.g., 1.5x).
- `total_amount = sum(seat_price for each seat)`.

---

## 10. Concurrency & Overbooking Prevention

### 10.1 Problem
Two users simultaneously try to book the **same seat** for the same showtime.

### 10.2 Strategies (Choose one or combine)

#### **Strategy A: Database Unique Constraint**
- Add a `UNIQUE(showtime_id, seat_id)` constraint on `ReservationSeat` (filtered for CONFIRMED).
- Second insert will throw a unique-violation error → handle gracefully.

#### **Strategy B: Pessimistic Locking**
```sql
BEGIN;
SELECT * FROM seats WHERE id IN (...) FOR UPDATE;
-- check availability
INSERT INTO reservations ...;
COMMIT;
```

#### **Strategy C: Optimistic Locking with Version Column**
- Add `version` column; check & increment on update.

#### **Strategy D: Application-Level Mutex / Redis Lock**
- Use distributed lock (e.g., Redis `SETNX`) keyed by `showtime_id:seat_id`.

> ✅ **Recommended:** Combine **Strategy A (DB constraint)** + **Strategy B (transactional locking)** for strongest guarantees.

### 10.3 Booking Workflow
1. Begin transaction.
2. Lock requested seats (`SELECT ... FOR UPDATE`).
3. Verify seats are free for that showtime.
4. Insert reservation + reservation_seats.
5. Commit transaction.
6. On error → rollback & return 409 Conflict.

---

## 11. Authentication & Authorization

### 11.1 Authentication Flow (JWT)
1. User logs in → server validates credentials.
2. Server signs a JWT with payload `{ user_id, role, exp }`.
3. Client stores token & sends as `Authorization: Bearer <token>`.
4. Middleware verifies token on protected routes.

### 11.2 Authorization Middleware
- `authenticate` → ensures valid JWT.
- `authorize(role)` → ensures user has required role(s).

```
router.post('/movies', authenticate, authorize('ADMIN'), createMovie);
```

### 11.3 Password Security
- Hash via **bcrypt** (cost 10–12) or **argon2**.
- Never return password hash in API responses.

---

## 12. Reporting Module

### 12.1 Reports to Implement

#### **A. Revenue Report**
- Total revenue between two dates.
- Group by movie / day / week.
```sql
SELECT m.title, SUM(r.total_amount) AS revenue
FROM reservations r
JOIN showtimes s ON r.showtime_id = s.id
JOIN movies m ON s.movie_id = m.id
WHERE r.status = 'CONFIRMED'
  AND s.start_time BETWEEN :from AND :to
GROUP BY m.title;
```

#### **B. Capacity / Occupancy Report**
- For a given showtime: total seats, booked seats, % occupancy.

#### **C. Top-Grossing Movies**
- Order by `SUM(total_amount) DESC LIMIT 10`.

#### **D. Reservations Listing**
- Filterable by: user, movie, showtime, date range, status.

---

## 13. Error Handling & Validation

### 13.1 Standard Error Response
```json
{
  "success": false,
  "error": {
    "code": "SEAT_ALREADY_BOOKED",
    "message": "Seat A5 is already reserved for this showtime."
  }
}
```

### 13.2 HTTP Status Codes
| Code | Use Case |
|------|----------|
| 200 | OK |
| 201 | Created |
| 400 | Bad request / validation error |
| 401 | Unauthenticated |
| 403 | Unauthorized (forbidden) |
| 404 | Not found |
| 409 | Conflict (e.g., seat already booked) |
| 422 | Unprocessable entity |
| 500 | Server error |

### 13.3 Validation
- Use a schema validator (Joi / Zod / class-validator / Pydantic).
- Validate body, params, and query.

---

## 14. Tech Stack Recommendations

| Layer | Options |
|-------|---------|
| **Language** | Node.js, Python, Java, Go |
| **Framework** | Express/NestJS, FastAPI/Django, Spring Boot, Gin |
| **Database** | PostgreSQL (preferred), MySQL |
| **ORM** | Prisma, TypeORM, Sequelize, SQLAlchemy, Hibernate |
| **Auth** | JWT (jsonwebtoken / pyjwt) |
| **Validation** | Zod, Joi, class-validator, Pydantic |
| **Testing** | Jest, pytest, JUnit |
| **Docs** | Swagger / OpenAPI |
| **Containerization** | Docker + docker-compose |

---

## 15. Project Structure (Example for Node.js)

```
movie-reservation-system/
├── src/
│   ├── config/             # DB, env config
│   ├── middlewares/        # auth, error handlers
│   ├── modules/
│   │   ├── auth/
│   │   ├── users/
│   │   ├── movies/
│   │   ├── genres/
│   │   ├── showtimes/
│   │   ├── reservations/
│   │   └── reports/
│   ├── utils/
│   ├── app.js
│   └── server.js
├── prisma/ (or migrations/)
├── seeds/
├── tests/
├── .env.example
├── docker-compose.yml
├── package.json
└── README.md
```

---

## 16. Seed Data

Seed script should populate:
1. **One default Admin** (e.g., `admin@example.com / Admin@123`).
2. A few **genres** (Action, Drama, Comedy, Sci-Fi, Horror).
3. Sample **movies** with posters.
4. One or two **halls** with predefined seat layouts (e.g., 10 rows × 10 seats).
5. Sample **showtimes** spanning the next 7 days.

---

## 17. Testing Strategy

### 17.1 Unit Tests
- Service layer logic (booking, cancellation, validation).

### 17.2 Integration Tests
- API endpoints with test database.
- Concurrency tests (simulate parallel bookings).

### 17.3 Edge Cases
- Booking same seat twice (concurrent).
- Cancelling a past reservation (should fail).
- Overlapping showtimes (should fail).
- Non-admin trying to access admin routes.
- Booking a seat in a cancelled showtime.

---

## 18. Deliverables

1. ✅ Source code in a Git repository.
2. ✅ `README.md` with:
   - Setup instructions
   - Environment variables list
   - How to run migrations & seeds
   - How to run tests
3. ✅ API documentation (Swagger or Postman collection).
4. ✅ ER diagram (image or .dbml).
5. ✅ Seed script with default admin.
6. ✅ Dockerfile + docker-compose (optional but recommended).

---

## 19. Optional Extensions

- 💳 **Payment integration** (Stripe / Razorpay sandbox).
- 📧 **Email notifications** on booking/cancellation (Nodemailer / SendGrid).
- 🔐 **OAuth login** (Google/Facebook).
- 📊 **Admin dashboard UI** (React/Next.js).
- 🎫 **QR code ticket generation**.
- 🌐 **WebSocket-based live seat selection** (real-time UI updates).
- 🔁 **Refund/credit system** for cancellations.
- 📍 **Multiple cinemas/locations** support.
- 🎟️ **Discount codes / promotions** module.
- 🌎 **Internationalization (i18n)**.

---

## 📝 Summary

This project simulates a **real-world, production-grade booking platform** that requires careful attention to:
- Schema design with M:N relationships
- Transactional integrity for concurrent operations
- Clean role-based authorization
- Aggregation queries for analytics

By the time you're done, you'll have a strong grasp of **enterprise backend engineering fundamentals** — schema design, concurrency control, RBAC, transactional logic, and reporting.

> 🎯 **Pro Tip:** Start with the data model — get the entities and relationships right before writing a single API route. Everything else flows from a clean schema.
# 🧪 CineReserve — Automated Test Suite

> **100+ test cases** across a 3-layer test pyramid: **RestAssured API tests**, **JUnit 5 unit + integration tests**, and **Selenium + Cucumber BDD UI tests** — all with **Allure HTML reporting**

---

## 📋 Test Suite Overview

### RestAssured API Tests (TestNG)

| Module | Test Class | Test Count |
|--------|-----------|-----------|
| Authentication | RegisterTests, LoginTests | **60** |
| Movies | GetMoviesTests, MovieCrudTests | **85** |
| Genres | GenreTests | **40** |
| Halls | HallTests | **45** |
| Showtimes | ShowtimeTests | **65** |
| Seat Holds | SeatHoldTests | **50** |
| Reservations | ReservationTests | **80** |
| Users | UserTests | **40** |
| Reports | ReportTests | **50** |
| Security | SecurityTests | **55** |
| Integration/E2E | EndToEndTests | **40** |
| **API TOTAL** | | **≥ 610** |

### JUnit 5 Unit Tests

| Test Class | Coverage |
|-----------|---------|
| AuthServiceTest | register, login — success + failure paths |
| ReservationServiceTest | create, cancel (full/partial refund), list |
| ShowtimeServiceTest | create, overlap detection, getById |
| HallServiceTest | create, update w/ and w/o future showtimes |

### Spring Boot Integration Tests

| Test Class | Coverage |
|-----------|---------|
| AuthControllerIntegrationTest | POST /api/auth/register + /login |
| UserControllerIntegrationTest | GET/PUT /api/users/me |
| ReservationControllerIntegrationTest | GET /api/reservations/me with filters |
| SecurityIntegrationTest | 401/403 enforcement for all protected routes |
| ReservationRepositoryTest | JPQL filter queries (status, date, title) |

### Selenium + Cucumber BDD UI Tests

| Feature File | Scenarios |
|-------------|----------|
| login.feature | Valid login, invalid credentials, redirect |
| signup-flow.feature | Register, duplicate email |
| home.feature | Landing page elements, navigation |
| movie-detail.feature | Movie info, watchlist toggle |
| booking-creation.feature | Seat grid, seat selection, hold creation |
| booking-success.feature | Confirmation page, view bookings link |
| bookings.feature | Filter bar, reservation list |
| profile.feature | Display name, loyalty section |
| admin-extended.feature | Admin nav, genres, reservations management |
| admin-movies.feature | Movie CRUD from admin panel |
| admin-halls.feature | Hall management |
| admin-users.feature | Role changes |

---

## 🛠️ Technology Stack

| Tool | Version | Purpose |
|------|---------|---------|
| **TestNG** | 7.10.2 | Test framework, groups, data providers |
| **RestAssured** | 5.5.0 | HTTP client for API calls |
| **Allure TestNG** | 2.29.0 | Rich HTML report generation |
| **Allure RestAssured** | 2.29.0 | HTTP request/response in reports |
| **Jackson** | 2.17.2 | JSON serialization/deserialization |
| **Java Faker** | 1.0.2 | Realistic random test data |
| **Log4j2** | 2.23.1 | Structured test logging |
| **AssertJ** | 3.26.3 | Fluent assertion library |
| **Lombok** | 1.18.34 | Boilerplate reduction |
| **AspectJ** | 1.9.22 | Required for Allure weaving |
| **Java** | 21 | Language version |
| **Maven** | 3.x | Build tool |

---

## 📁 Project Structure

```
APITests/
├── pom.xml                                          # Maven dependencies
├── testng-suites/
│   ├── full.xml                                     # All 560+ tests
│   ├── regression.xml                               # Regression suite
│   └── smoke.xml                                    # Critical path tests
├── src/test/
│   ├── java/com/cinereserve/api/
│   │   ├── base/
│   │   │   └── BaseTest.java                        # Setup, RestAssured config
│   │   ├── config/
│   │   │   └── ApiConfig.java                       # URLs, endpoints, constants
│   │   ├── utils/
│   │   │   ├── AuthUtil.java                        # Token management
│   │   │   └── TestDataBuilder.java                 # Request body factories
│   │   └── tests/
│   │       ├── auth/
│   │       │   ├── RegisterTests.java               # 30 tests
│   │       │   └── LoginTests.java                  # 30 tests
│   │       ├── movies/
│   │       │   ├── GetMoviesTests.java              # 25 tests
│   │       │   └── MovieCrudTests.java              # 60 tests
│   │       ├── genres/
│   │       │   └── GenreTests.java                  # 40 tests
│   │       ├── halls/
│   │       │   └── HallTests.java                   # 45 tests
│   │       ├── showtimes/
│   │       │   └── ShowtimeTests.java               # 65 tests
│   │       ├── reservations/
│   │       │   └── ReservationTests.java            # 80 tests
│   │       ├── users/
│   │       │   └── UserTests.java                   # 40 tests
│   │       ├── reports/
│   │       │   └── ReportTests.java                 # 50 tests
│   │       ├── security/
│   │       │   └── SecurityTests.java               # 55 tests
│   │       └── integration/
│   │           └── EndToEndTests.java               # 40 tests
│   └── resources/
│       ├── allure.properties                        # Allure config
│       ├── test.properties                          # API URL & credentials
│       └── log4j2.xml                              # Logging config
└── target/
    ├── allure-results/                              # Raw Allure data
    └── surefire-reports/                            # TestNG reports
```

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Backend running at `http://localhost:8080`
- Allure CLI (optional, for serving reports)

### 1. Install Allure CLI (once)
```bash
# Windows (Scoop)
scoop install allure

# macOS (Homebrew)
brew install allure

# Linux
npm install -g allure-commandline
```

### 2. Start the Backend First
```bash
cd MovieReservationSystem
mvn spring-boot:run
```

### 3. Run Tests
```bash
# Run full suite
cd APITests
mvn clean test

# Run smoke tests only
mvn clean test -Dsurefire.suiteXmlFiles=testng-suites/smoke.xml

# Run regression suite
mvn clean test -Dsurefire.suiteXmlFiles=testng-suites/regression.xml

# Run specific test class
mvn clean test -Dtest=RegisterTests

# Run against different environment
mvn clean test -Dapi.base.url=http://staging:8080
```

### 4. Generate & View Allure Report
```bash
# Generate report
mvn allure:report

# Open in browser (auto-serves at localhost:PORT)
mvn allure:serve

# Or open HTML directly
start target/site/allure-maven-plugin/index.html   # Windows
open target/site/allure-maven-plugin/index.html    # macOS
```

---

## 🔧 Configuration

### `src/test/resources/test.properties`
```properties
api.base.url=http://localhost:8080
connect.timeout.ms=10000
read.timeout.ms=30000
admin.email=admin@cinereserve.com
admin.password=Admin@123
```

Override at runtime:
```bash
mvn test -Dapi.base.url=http://production:8080
```

---

## 📊 Test Categories

### 🟢 Positive Tests
- Happy path for all CRUD operations
- Valid data inputs
- Correct HTTP status codes (200/201)

### 🔴 Negative Tests
- Missing required fields → 400
- Invalid data formats → 400
- Non-existent resources → 404
- Wrong password → 401
- Duplicate data → 409

### 🔐 Security Tests
- JWT token validation
- Role-Based Access Control (RBAC)
- SQL injection prevention
- XSS prevention
- Authentication bypass attempts
- Privilege escalation attempts
- Data exposure checks

### 🔄 Integration / E2E Tests
- Complete user booking journey
- Admin management flow
- Seat availability lifecycle
- Concurrent booking (race condition)
- Revenue report after booking

### ⚡ Performance
- All endpoints assert response time < 5 seconds

---

## 🎯 Test Coverage

| Endpoint | Methods Tested | Auth Levels |
|----------|----------------|-------------|
| POST /api/auth/register | Valid, invalid, duplicate, security | Public |
| POST /api/auth/login | Valid, invalid, JWT format | Public |
| GET /api/movies | List, filter by genre/status | Public |
| GET /api/movies/{id} | Valid, 404, invalid ID | Public |
| POST /api/movies | Create, validate, auth | Admin |
| PUT /api/movies/{id} | Update, validate, auth | Admin |
| DELETE /api/movies/{id} | Soft delete, auth, 404 | Admin |
| GET/POST/DELETE /api/genres | Full CRUD | Mixed |
| GET/POST/PUT/DELETE /api/halls | Full CRUD | Mixed |
| GET/POST/PUT/DELETE /api/showtimes | Full CRUD + overlap | Mixed |
| GET /api/showtimes/{id}/seats | Availability, auth | User |
| POST /api/reservations | Book, concurrency, atomicity | User |
| GET /api/reservations/me | Own reservations | User |
| DELETE /api/reservations/{id} | Cancel, auth, past | User |
| GET /api/reservations | All (admin) | Admin |
| GET /api/users/me | Profile, auth | User |
| GET /api/users | List all | Admin |
| PATCH /api/users/{id}/role | Promote/demote | Admin |
| GET /api/reports/revenue | Revenue, filters | Admin |
| GET /api/reports/capacity/{id} | Occupancy stats | Admin |
| GET /api/reports/top-movies | Top 10 | Admin |

---

## 📈 Allure Report Features

After running tests, the Allure report includes:
- **Suite** view by feature/story
- **Behavior** view (Epic → Feature → Story)
- **Request/Response** details for each test
- **Timeline** of test execution
- **Severity** levels (BLOCKER → TRIVIAL)
- **Retry** history
- **Categories** of failures

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|---------|
| `401 Unauthorized` on all tests | Check backend is running on port 8080 |
| `Connection refused` | Start the Spring Boot backend first |
| Admin token fails | Verify seed data: `admin@cinereserve.com / Admin@123` |
| Allure report empty | Run `mvn allure:report` after `mvn test` |
| Tests skip with null | Backend may not have seeded data |

---

## 📝 Commit History Integration

This test suite was committed to git alongside the application code:
```
feat: add comprehensive API test automation suite
- 560+ test cases across 10 test classes
- TestNG + RestAssured + Allure Reports
- Security, integration, and concurrency tests
- Full CRUD coverage for all endpoints

test: add UI automation test coverage
- Selenium + Cucumber BDD across 12 feature files
- Page Object pattern (LoginPage, HomePage, MoviePage, ProfilePage, etc.)
- Allure HTML reports with screenshots on failure

test: add unit tests for services
- JUnit 5 + Mockito for AuthService, ReservationService, ShowtimeService, HallService

test: add integration tests
- @SpringBootTest MockMvc tests (Auth, User, Reservation controllers)
- SecurityIntegrationTest (401/403 enforcement)
- ReservationRepositoryTest (JPQL filter queries)
```

---

**Author**: Anuj Rawat | **Version**: 1.1.0 | **Date**: June 6, 2026


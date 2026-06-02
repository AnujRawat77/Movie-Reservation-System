# CineReserve — Automation Test Results

> Run Date: 2026-06-02  
> Backend: Spring Boot on `http://localhost:8080`  
> Frontend: Vite/React on `http://localhost:5173`

---

## ✅ Summary

| Suite         | Profile        | Tests | Passed | Failed | Skipped | Time     |
|---------------|----------------|-------|--------|--------|---------|----------|
| API Full      | `api-tests`    | 560   | 560    | 0      | 0       | ~25s     |
| Smoke         | `smoke`        | 7     | 7      | 0      | 0       | ~17s     |
| Regression    | `regression`   | 583   | 583    | 0      | 0       | ~134s    |
| UI Full       | `ui-tests`     | 23    | 23     | 0      | 0       | ~114s    |
| BDD Cucumber  | `bdd-tests`    | 20    | 20     | 0      | 0       | ~99s     |

**Total: 1193 test executions — 100% pass rate** 🎉

---

## How to Run

```bash
# API tests (default)
mvn test

# Smoke suite
mvn test -P smoke

# Regression suite
mvn test -P regression

# UI Selenium tests (requires frontend running on port 5173)
mvn test -P ui-tests

# BDD Cucumber tests (requires frontend running on port 5173)
mvn test -P bdd-tests

# Generate Allure HTML report
mvn allure:report
# Report generated at: target/site/allure-maven-plugin/index.html

# Open report in browser (starts embedded server)
mvn allure:serve
```

---

## Test Coverage Areas

### API Tests (560)
- **Auth**: Register, Login — 30+ tests each
- **Movies**: GET list, CRUD operations — 80+ tests
- **Genres**: CRUD, validation — 40+ tests
- **Halls**: CRUD, seating — 40+ tests
- **Showtimes**: CRUD, scheduling, overlap detection — 65 tests
- **Reservations**: Create, cancel, pricing, seats — 80+ tests
- **Users**: Profile, admin role management — 40+ tests
- **Reports**: Revenue reports — 30+ tests
- **Security**: Auth bypass attempts, token validation — 40+ tests
- **Integration (E2E)**: End-to-end booking flows — 30+ tests

### UI Tests (23 via Selenium)
- Login page load, admin login redirect
- Home page (movies list)
- Admin panel navigation and operations

### BDD Tests (20 Cucumber scenarios)
- `login.feature` — Login page scenarios
- `movies.feature` — Movie browsing scenarios
- `signup.feature` — Registration page scenarios
- `admin.feature` — Admin panel scenarios

---

## Allure Report

Generated at: `target/site/allure-maven-plugin/index.html`

The report includes:
- Test steps with request/response details (via AllureRestAssured filter)
- Epics / Features / Stories hierarchy
- Severity levels (BLOCKER → MINOR)
- Timeline view with test durations
- Screenshots on UI test failures


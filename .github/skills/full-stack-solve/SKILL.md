---
name: full-stack-solve
description: "Full-stack problem solver for the Movie Reservation System. Use when: given a bug, missing feature, or broken behavior that spans backend (Spring Boot) and/or frontend (React/TanStack). Resolves the problem end-to-end: implements the fix in backend Java code and/or frontend TypeScript/React code, locates and runs existing tests, and writes new unit/integration/API/UI tests when coverage is missing. Triggers: 'solve this problem', 'fix this bug', 'implement this feature', 'make the tests pass', 'full-stack fix', 'integrate frontend and backend'."
argument-hint: "Describe the problem, bug, or feature to implement"
---

# Full-Stack Problem Solver

## Purpose
Given a problem description, resolve it completely across the Spring Boot backend and/or the React/TanStack frontend, ensure tests exist and pass, and leave no loose ends.

---

## Stack Reference

| Layer | Location | Language | Run Command |
|---|---|---|---|
| Backend (Spring Boot 4 / Java 21) | `MovieReservationSystem/` | Java | `.\mvnw.cmd spring-boot:run` |
| Backend unit tests | `MovieReservationSystem/src/test/` | JUnit 5 | `.\mvnw.cmd test` |
| Frontend (Vite + React + TanStack) | `Frontend/` | TypeScript/React | `npm run dev` |
| API automation tests | `AutomationTests/src/test/java/com/cinereserve/api/` | RestAssured + TestNG | `mvn test -Dsuite=regression` |
| UI automation tests | `AutomationTests/src/test/java/com/cinereserve/ui/` | Selenium + Cucumber | `mvn test -Dsuite=bdd` |

Backend runs on **http://localhost:8080**. Frontend runs on **http://localhost:5173** (or 8081 if 5173 is taken).

---

## Procedure

### Step 1 — Understand the Problem
1. Read the full problem statement carefully.
2. Identify **which layer(s)** are affected:
   - Backend only (logic, security, API, DB)
   - Frontend only (UI, state, routing, API call)
   - Both (end-to-end flow)
3. Locate relevant files using `grep_search` or `semantic_search` before touching anything.
4. Read those files fully before editing — never guess at existing code.

### Step 2 — Trace the Code Path
For a backend problem, trace: `Controller → Service → Repository → Entity`
For a frontend problem, trace: `Route/Page → Hook → lib/api.ts → Backend`
For full-stack: trace both ends and identify the contract boundary (the REST endpoint + DTO).

### Step 3 — Fix the Backend (if affected)
1. Apply the minimal, correct change.
2. If a new endpoint or field is needed, update:
   - Entity (if schema change)
   - DTO (request/response)
   - Repository (if new query)
   - Service (business logic)
   - Controller (endpoint mapping)
3. Do NOT change security config unless the problem is auth/access-related.
4. Verify CORS allows `http://localhost:5173` and `http://localhost:8081`.

### Step 4 — Fix the Frontend (if affected)
1. Apply the minimal, correct change.
2. If a new API call is needed, add it to `Frontend/src/lib/api.ts` first.
3. Update the relevant route file in `Frontend/src/routes/`.
4. Use existing hooks in `Frontend/src/hooks/` — don't duplicate logic.
5. Use existing UI primitives from `Frontend/src/components/ui/`.

### Step 5 — Test Coverage Check
1. Search for existing tests related to the changed behavior:
   - Backend unit: `MovieReservationSystem/src/test/java/`
   - API tests: `AutomationTests/src/test/java/com/cinereserve/api/tests/<domain>/`
   - UI/BDD tests: `AutomationTests/src/test/java/com/cinereserve/ui/`
2. **If tests exist** — read them and confirm they will exercise the fixed code path. If they need updating, update them.
3. **If no tests exist** — write them before declaring the task done (see test guidelines below).

### Step 6 — Write Missing Tests

#### Backend Unit Test (JUnit 5 + Spring Boot Test)
- Place in `MovieReservationSystem/src/test/java/com/movie_reservation/MovieReservationSystem/<domain>/`
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` for controller tests or `@DataJpaTest` for repo tests.
- Name: `<TestedClass>Test.java`
- Cover: happy path, edge cases, expected exceptions/error codes.

#### API Automation Test (RestAssured + TestNG)
- Place in `AutomationTests/src/test/java/com/cinereserve/api/tests/<domain>/`
- Extend `BaseApiTest`.
- Use the existing `ApiUtils` / auth helpers to get tokens.
- Name: `<Feature>ApiTest.java`
- Cover: 200 success, 400/404/403/409 error cases.

#### UI/BDD Test (Selenium + Cucumber)
- Feature file: `AutomationTests/src/test/resources/features/`
- Steps: `AutomationTests/src/test/java/com/cinereserve/ui/steps/`
- Page objects: `AutomationTests/src/test/java/com/cinereserve/ui/pages/`
- Only write UI tests for user-facing flows; skip for internal API-only changes.

### Step 7 — Run and Verify

Run backend tests first (faster, no browser needed):
```
cd MovieReservationSystem
.\mvnw.cmd test
```

Run API automation tests:
```
cd AutomationTests
mvn test -Dsuite=regression
```

Run UI tests (requires backend + frontend running):
```
cd AutomationTests
mvn test -Dsuite=bdd
```

All tests must be **GREEN** before the task is complete. If a test fails:
- Read the failure output carefully.
- Fix the root cause in production code or the test (if the test assertion was wrong).
- Re-run until green.

### Step 8 — Integration Smoke Check
1. Confirm backend is running on 8080.
2. Confirm frontend is running on 5173/8081.
3. Manually verify the fixed flow end-to-end if a UI change is involved.

---

## Quality Gates (must all pass before done)

- [ ] Root cause identified and clearly understood
- [ ] Fix applied at the correct layer(s) with no unnecessary changes
- [ ] All existing related tests still pass
- [ ] New tests written if no coverage existed
- [ ] New tests are GREEN
- [ ] Frontend API call matches backend response shape exactly
- [ ] No hardcoded secrets, tokens, or localhost URLs in production code

---

## Common Patterns in This Codebase

### Adding a new API field end-to-end
1. Add field to Entity → JPA auto-migrates (H2, create-drop)
2. Add to response DTO builder in Service
3. Add to TypeScript type in `Frontend/src/lib/api.ts`
4. Use in the component

### Auth/role guard
- Backend: `@PreAuthorize("hasRole('ADMIN')")` or check in SecurityConfig
- Frontend: check `user.role === 'ADMIN'` from `useAuth` hook

### Error handling
- Backend throws: `BusinessException(code, message)` or `ResourceNotFoundException`
- Frontend catches: `ApiError` (has `.message` and `.status` fields) — show via `toast.error()`

### Adding a new test user
- Add in `DataSeeder.java` `seedUsers()` method
- Password must be encoded with `passwordEncoder.encode()`

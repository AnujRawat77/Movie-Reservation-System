---
name: restart-application-session
description: "Check whether a previous Movie Reservation System app session is still running, stop stale backend/frontend processes, and start the application cleanly. Use when: app ports are occupied, a previous run is stuck, local startup fails due to orphan sessions, or you want a clean restart. Triggers: 'restart app', 'stop previous session and start app', 'check running app session', 'clean start backend', 'restart frontend and backend'."
argument-hint: "Choose startup target: backend-only or full-stack"
---

# Restart Application Session

## Purpose
Provide a reliable, repeatable workflow to:
1. Detect already running app sessions.
2. Stop those sessions safely.
3. Start the app from a clean state.

Applies to this repository structure:
- Backend: MovieReservationSystem (Spring Boot)
- Frontend: Frontend (Vite/React)

## When To Use
- Port conflicts on 8080, 8081, or 5173.
- Startup says address already in use.
- Previous terminals were closed but processes stayed alive.
- You need a deterministic clean restart before testing.

## Inputs
- Startup target:
  - backend-only
  - full-stack

If no target is provided, default to backend-only.

## Procedure

### Step 1: Detect Existing Sessions
1. Check listeners on common app ports: 8080, 8081, 5173, 5174, 3000, 4200.
2. Map listening PIDs to process details.
3. If no listeners exist, continue directly to startup.

Example PowerShell:

```powershell
$ports = 8080,8081,5173,5174,3000,4200
Get-NetTCPConnection -State Listen |
  Where-Object { $ports -contains $_.LocalPort } |
  Select-Object LocalAddress, LocalPort, OwningProcess
```

### Step 2: Stop Previous Sessions
1. Collect unique OwningProcess IDs from Step 1.
2. Force-stop each process tree.
3. Re-check the same ports.
4. If any port is still occupied, repeat one more time and inspect parent launcher processes.

Example PowerShell:

```powershell
$targetPids = @(/* replace with detected pids */)
foreach ($pid in $targetPids) {
  taskkill /PID $pid /T /F
}
```

### Step 3: Start Application
Decision branch:

1. backend-only
- Working directory: MovieReservationSystem
- Command: .\\mvnw.cmd spring-boot:run
- Expected healthy state: backend listening on 8080.

2. full-stack
- Start backend first:
  - MovieReservationSystem -> .\\mvnw.cmd spring-boot:run
- Then start frontend in separate terminal:
  - Frontend -> npm run dev
- Expected healthy state:
  - Backend on 8080
  - Frontend on 5173 (or fallback port shown by Vite)

### Step 4: Verify Clean Startup
1. Confirm expected listener ports are active.
2. Confirm startup logs indicate successful boot:
- Spring Boot: application started without bind errors.
- Vite: dev server URL printed.
3. If startup fails due to port-in-use, return to Step 1.

## Decision Points
- Existing listeners found?
  - Yes: stop them before any new startup.
  - No: start immediately.
- User requested backend-only or full-stack?
  - backend-only: start Spring Boot only.
  - full-stack: start Spring Boot then Vite.
- Port still occupied after first kill pass?
  - Run second kill pass and inspect launcher processes.

## Quality Criteria
- No stale listener remains on app-related ports before startup.
- Startup command exits neither immediately nor with bind errors.
- Backend is reachable on 8080.
- For full-stack mode, frontend is reachable on reported Vite port.

## Completion Checklist
- [ ] Checked app-related listening ports
- [ ] Stopped all stale session processes
- [ ] Re-verified ports are clear before startup
- [ ] Started requested app target (backend-only or full-stack)
- [ ] Verified healthy listeners/logs after startup

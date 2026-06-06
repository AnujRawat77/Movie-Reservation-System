# Setup & Installation Guide
## Prerequisites
### Required Software
- **JDK 21** or later
- **Maven 3.8+** (included as mvnw)
- **Node.js 18+** or Bun runtime
- **npm or yarn** or **bun**
- **Git 2.30+**
### Optional but Recommended
- **Docker** & **Docker Compose** (for containerization)
- **PostgreSQL** (for production database)
- **Postman** or **Insomnia** (for API testing)
- **Visual Studio Code** or **JetBrains IntelliJ**
---
## Backend Setup (Spring Boot)
### 1. Navigate to Backend Directory
\\\ash
cd MovieReservationSystem
\\\
### 2. Build with Maven
\\\ash
./mvnw clean install
# On Windows: mvnw.cmd clean install
\\\
### 3. Run the Application
\\\ash
./mvnw spring-boot:run
# Or: java -jar target/MovieReservationSystem-0.0.1-SNAPSHOT.jar
\\\
### 4. Verify Backend is Running
- API Base: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console
---
## Frontend Setup (React)
### 1. Navigate to Frontend Directory
\\\ash
cd Frontend
\\\
### 2. Install Dependencies
Using npm:
\\\ash
npm install
\\\
Using Bun (faster):
\\\ash
bun install
\\\
### 3. Create Environment File
\\\ash
# Frontend/.env
VITE_API_BASE_URL=http://localhost:8080
\\\
### 4. Run Development Server
\\\ash
npm run dev
# Or with Bun: bun run dev
\\\
### 5. Verify Frontend is Running
- Frontend: http://localhost:5173
- Development server should auto-reload on changes
---
## Test Credentials
### Default Admin User
`
Email: admin@cinereserve.com
Password: Admin@123
Role: ADMIN
`
### Sample Regular User (if seeded)
`
Email: user@example.com
Password: User@123
Role: USER
`
---
## Database Configuration
### H2 Database (Development)
- **Console URL**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:mem:moviedb
- **Username**: sa
- **Password**: password
Configuration in: \MovieReservationSystem/src/main/resources/application.properties\
### Switch to PostgreSQL (Production)
1. Install PostgreSQL
2. Create database: \createdb movie_reservation_db\
3. Update \pplication.properties\:
   \\\properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/movie_reservation_db
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=validate
   \\\
4. Run: \./mvnw spring-boot:run\
---
## API Testing
### Using Swagger UI
1. Go to http://localhost:8080/swagger-ui.html
2. Click on endpoint to expand
3. Click "Try it out"
4. Fill in required fields
5. Click "Execute"
### Using cURL
\\\ash
# Register
curl -X POST http://localhost:8080/api/auth/register \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "Password@123"
  }'
# Login
curl -X POST http://localhost:8080/api/auth/login \\
  -H "Content-Type: application/json" \\
  -d '{
    "email": "admin@cinereserve.com",
    "password": "Admin@123"
  }'
# Get Movies
curl http://localhost:8080/api/movies
\\\
---
## Complete Development Workflow
### Terminal 1: Backend
\\\ash
cd MovieReservationSystem
./mvnw spring-boot:run
\\\
### Terminal 2: Frontend
\\\ash
cd Frontend
npm install
npm run dev
\\\
### Access Application
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger Docs: http://localhost:8080/swagger-ui.html
---
## Troubleshooting
### Port 8080 Already in Use
\\\ash
# Find process using port 8080
lsof -i :8080  # On macOS/Linux
# Or in PowerShell (Windows):
netstat -ano | findstr :8080
# Kill the process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows
\\\
### Port 5173 Already in Use
\\\ash
# Change port in vite.config.ts
# Or kill process using netstat/lsof
\\\
### Maven Build Issues
\\\ash
# Clean and rebuild
./mvnw clean
./mvnw install
# Skip tests
./mvnw clean install -DskipTests
\\\
### Frontend Module Not Found
\\\ash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
\\\
### CORS Errors
- Check backend \cors.allowed-origins\ in application.properties
- Ensure frontend URL matches (default: http://localhost:5173)
### JWT Token Errors
- Token expires after 24 hours
- Login again to refresh token
- Check Authorization header format: \Bearer <token>\
---
## Building for Production
### Backend
\\\ash
./mvnw clean package
java -jar target/MovieReservationSystem-0.0.1-SNAPSHOT.jar
\\\
### Frontend
\\\ash
npm run build
npm run preview  # Preview production build
# Output in: dist/ folder
\\\
---
## Docker Setup (Optional)
### Docker Compose
\\\yaml
# docker-compose.yml
version: '3.8'
services:
  backend:
    build: ./MovieReservationSystem
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/moviedb
  frontend:
    build: ./Frontend
    ports:
      - "5173:5173"
  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=moviedb
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data
volumes:
  postgres_data:
\\\
Run: `docker-compose up`

---

## Running Tests

### Backend Unit & Integration Tests
```bash
cd MovieReservationSystem
./mvnw test
```

### RestAssured API Tests (requires app running on :8080)
```bash
cd AutomationTests
# Full suite
mvn test -Dsuite=full

# Smoke only
mvn test -Dsuite=smoke

# Regression
mvn test -Dsuite=regression
```

### UI Automation Tests (requires Chrome + app running)
```bash
cd AutomationTests
mvn test -Dsuite=ui-full
```

### Generate Allure Report
```bash
cd AutomationTests
mvn allure:serve   # opens browser automatically
```

---

**Version**: 1.1.0
**Last Updated**: June 6, 2026

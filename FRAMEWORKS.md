# Technologies & Frameworks Used
## Backend Framework
### Spring Boot 4.0.6
- **Purpose**: Full-stack Java framework for rapid application development
- **Version**: 4.0.6 (Latest stable)
- **Java Version**: 21 (Latest LTS)
#### Core Dependencies
**Spring Web**
- REST API development
- HTTP request/response handling
- Built-in server (Tomcat)
**Spring Data JPA**
- Object-relational mapping (ORM)
- Repository pattern implementation
- Automatic CRUD repository generation
- Transaction management
**Spring Security**
- Authentication framework
- Authorization and access control
- JWT token validation
- CORS support
- Password encoding (BCrypt)
**Spring Validation**
- Bean validation annotations (@Valid, @NotNull, etc.)
- Custom validators
- Input validation before processing
**Spring Boot DevTools**
- Hot reload during development
- faster feedback loop
### JWT (JSON Web Tokens)
**Library**: io.jsonwebtoken (jjwt)
- Version: 0.12.6
- HS256 algorithm for token signing
- Token expiration (24 hours default)
- Stateless authentication
### Password Security
**BCrypt**
- Cost factor: 12 (2^12 iterations)
- Adaptive hashing algorithm
- Random salt generation
- Industry standard for password hashing
### Database (Development)
**H2 Database**
- In-memory relational database
- Zero configuration
- Embedded SQL engine
- Web console at /h2-console
- Perfect for development and testing
**JPA/Hibernate**
- Object-Relational Mapping (ORM)
- SQL generation from entity models
- Lazy/Eager loading strategies
- Transaction management
- Cascade operations
### API Documentation
**SpringDoc OpenAPI**
- Version: 2.8.8
- Automatic Swagger UI generation
- OpenAPI 3.0 specification
- Interactive API testing
- Located at /swagger-ui.html
### Utilities
**Lombok**
- Code generation library
- @Data, @Builder, @RequiredArgsConstructor
- Reduces boilerplate code
- Annotation processing
---
## Frontend Framework
### React 19.2.0
- **React Server Components** (experimental)
- **React Hooks** for state management
- **Concurrent Features** for better performance
- JSX syntax for component structure
### TanStack Router 1.168.25
- **Type-safe routing** with TypeScript
- **File-based routing** automation
- **Route parameters & search params**
- **Lazy route loading**
- **History management**
### TanStack React Query 5.83.0
- **Server state management**
- **Caching and synchronization**
- **Automatic refetching**
- **Request deduplication**
- **Background updates**
### Tailwind CSS 4.2.1
- **Utility-first CSS framework**
- **JIT compilation** for optimal bundle size
- **Dark mode support**
- **Responsive design** (mobile-first)
- **Custom configuration** via tailwind.config.js
### Radix UI
- **Accessible component library**
- **Built on WAI-ARIA standards**
- **Headless UI components**
- **Components included**:
  - Dialog, Popover, Accordion
  - Dropdown Menu, Context Menu
  - Sheet (Drawer), Sidebar
  - Various form inputs and indicators
### React Hook Form 7.71.2
- **Lightweight form validation**
- **Minimal re-renders**
- **Built-in validation**
- **Custom validators support**
- **TypeScript integration**
### TypeScript 5.8.3
- **Static type checking**
- **IntelliSense support**
- **Better IDE experience**
- **Type safety for API calls**
- **Interface definitions**
### Vite 7.3.1
- **Next-generation build tool**
- **Lightning-fast HMR** (Hot Module Replacement)
- **ESM native**
- **Optimized build output**
- **Development server**
### UI Enhancements
**Framer Motion**
- Smooth animations
- Gesture animations
- Layout animations
**Sonner**
- Toast notifications
- Error/success messages
**Lucide React**
- Icon library
- 1000+ SVG icons
**Canvas-Confetti**
- Celebration animations
- Booking confirmation animations
---
## Database Schema
### Relational Database Design
- **Normalization**: 3NF (Third Normal Form)
- **Relationships**: 1:N, M:N with junction tables
- **Constraints**: PK, FK, UNIQUE, CHECK
- **Indexes**: Foreign keys, frequently queried fields
- **Soft Delete**: Movies marked with isDeleted flag
### Entity Relationship Model
`
8 Core Tables:
- users (User accounts)
- movies (Movie catalog)
- genres (Genre categories)
- movie_genre (M:N junction)
- halls (Theater halls)
- seats (Individual seats)
- showtimes (Movie schedules)
- reservations (Bookings)
- reservation_seats (Booked seats)
`
---
## Development Tools
### Build & Dependency Management
**Maven** (Backend)
- Declarative dependency management
- Multi-module support
- Plugin ecosystem
- pom.xml configuration
**npm** (Frontend)
- Package management
- Script automation
- Bun runtime (alternative to Node.js)
### Version Control
**Git**
- Distributed version control
- GitHub-compatible
- Branch management
- Commit history
### Code Quality
**ESLint**
- JavaScript/TypeScript linting
- Code style enforcement
- Plugin-based architecture
---
## Architecture Patterns
### Backend Patterns
- **Repository Pattern**: Data access abstraction
- **Service Pattern**: Business logic layer
- **DTO Pattern**: Data transfer objects
- **Controller Pattern**: HTTP request handling
- **Dependency Injection**: Spring IoC container
- **Transaction Pattern**: @Transactional management
### Frontend Patterns
- **Component-Based**: Reusable UI components
- **Hook Pattern**: Stateful logic in functions
- **Context API**: Global state management
- **Custom Hooks**: Reusable component logic
- **Container & Presentational**: Smart/dumb components
---
## Security Technologies
### Authentication
- **JWT (JSON Web Tokens)**
- **BCrypt password hashing**
- **Token-based stateless auth**
- **Bearer token in Authorization header**
### Authorization
- **Role-Based Access Control (RBAC)**
- **Spring Security method-level security**
- **Endpoint access restrictions**
- **Decorator-based (Spring @PreAuthorize)**
### Data Protection
- **HTTPS/TLS** (recommended for production)
- **Parameterized queries** (SQL injection prevention)
- **Input validation** at all layers
- **CORS configuration**
---
## Testing & Quality Assurance
### Testing Frameworks
- **JUnit 5**: Backend unit testing
- **Mockito**: Mocking framework
- **TestNG**: Advanced testing (optional)
### Code Coverage
- JaCoCo for coverage reports
- Target: 80%+ critical path coverage
---
## Performance Optimization
### Backend
- **Connection pooling**: HikariCP (default)
- **Query optimization**: Indexes on FK and frequently queried fields
- **Lazy loading**: M:N relationships
- **Caching ready**: Cacheable annotations
### Frontend
- **Code splitting**: Route-based lazy loading
- **Tree shaking**: Unused code removal
- **Minification**: Vite production build
- **Compression**: Gzip compression ready
---
## Deployment & DevOps
### Containerization (Additional)
- **Docker**: Container images
- **Docker Compose**: Multi-container orchestration
### CI/CD (Recommended)
- **GitHub Actions**: Automated testing
- **Maven Central**: Dependency management
---
**Last Updated**: May 31, 2026
**Version**: 1.0.0

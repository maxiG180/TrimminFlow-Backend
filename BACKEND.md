# TRIMMINFLOW BACKEND

## Overview

Production-ready Spring Boot backend for TRIMMINFLOW - a barbershop management SaaS platform. Implements RESTful APIs with PostgreSQL database, JWT authentication foundation, and comprehensive validation.

## What's Implemented

### 1. **Barbershop & Owner Registration** (`POST /api/v1/auth/register`)

- **Full-stack registration endpoint** with production-grade validation
- **Backend Validation**: Jakarta Bean Validation annotations
- **OWASP-compliant password requirements**:
  - Minimum 8 characters
  - At least 1 uppercase letter
  - At least 1 lowercase letter
  - At least 1 number
  - At least 1 special character (`!@#$%^&*(),.?":{}|<>`)
- **Transactional operations**: Creates both `Barbershop` and `User` (owner) entities atomically
- **BCrypt password hashing**: Secure password storage
- **Duplicate email detection**: Prevents duplicate registrations
- **Structured error responses**: Field-specific validation errors via `GlobalExceptionHandler`

### 2. **Entity Management**

**Barbershop Entity:**
- UUID primary key
- Name, email, phone, address
- Business hours (JSONB)
- Timezone support
- QR code URL storage
- Automatic timestamp tracking (`@PrePersist`, `@PreUpdate`)

**User Entity:**
- UUID primary key
- Email (unique, validated)
- First name & last name (pattern validated)
- BCrypt password hash
- Role-based access (ADMIN, BARBER, RECEPTIONIST)
- Barbershop relationship
- Last login tracking
- Automatic timestamp tracking

### 3. **CRUD APIs**

**Barbershop Controller** (`/api/v1/barbershops`):
- `GET /` - List all barbershops
- `GET /{id}` - Get barbershop by ID
- `POST /` - Create new barbershop
- `PUT /{id}` - Update barbershop
- `DELETE /{id}` - Delete barbershop

**User Controller** (`/api/users`):
- `GET /` - List all users
- `GET /{id}` - Get user by ID
- `GET /email/{email}` - Get user by email
- `POST /` - Create new user
- `PUT /{id}` - Update user
- `DELETE /{id}` - Delete user

### 4. **Security Configuration**

- **Spring Security** integration
- **BCrypt password encoding** (strength 10)
- **CORS configuration** for Next.js frontend (ports 3000, 5173)
- **JWT foundation** ready (configuration in place, not yet implemented)
- Development security: Generated password on startup

### 5. **API Documentation**

- **Swagger/OpenAPI 3.0** integration
- Interactive API docs at `/swagger-ui.html`
- API explorer at `/v3/api-docs`
- Annotated controllers with `@Operation` and `@Tag`

### 6. **Database**

- **PostgreSQL 17.6** with Docker Compose
- **Hibernate ORM** with automatic schema updates (dev mode)
- **Flyway** configured but disabled (manual schema for now)
- **JSONB support** for business hours
- **UUID primary keys** for all entities

---

## Tech Stack

### Core Framework
- **Spring Boot** 3.5.5
- **Java** 17+
- **Gradle** 8.14

### Dependencies
```gradle
// Web & Data
spring-boot-starter-web           // REST API
spring-boot-starter-data-jpa      // Hibernate ORM
spring-boot-starter-validation    // Jakarta Bean Validation
spring-boot-starter-security      // Security & BCrypt

// Database
postgresql                        // PostgreSQL driver
flyway-core                       // Database migrations (disabled)
hypersistence-utils-hibernate-63  // JSONB support

// Documentation
springdoc-openapi-starter-webmvc-ui  // Swagger UI

// Authentication (configured, not yet used)
jjwt-api:0.12.3                   // JWT tokens
jjwt-impl:0.12.3
jjwt-jackson:0.12.3

// Third-party integrations (configured, not yet used)
stripe-java:24.16.0               // Payment processing
resend-java:3.0.0                 // Email notifications
google.zxing:core:3.5.2           // QR code generation
google.zxing:javase:3.5.2

// Development
spring-boot-devtools              // Hot reload
```

---

## Project Structure

```
demo/
├── src/main/java/com/trimminflow/demo/
│   ├── DemoApplication.java             # Main application class
│   ├── config/
│   │   ├── GlobalExceptionHandler.java  # Validation error handling
│   │   ├── SecurityConfig.java          # Spring Security + BCrypt
│   │   └── WebConfig.java               # CORS configuration
│   ├── controller/
│   │   ├── AuthController.java          # Registration endpoint
│   │   ├── BarbershopController.java    # Barbershop CRUD
│   │   └── UserController.java          # User CRUD
│   ├── dto/
│   │   ├── RegisterRequest.java         # Registration input DTO (validated)
│   │   └── RegisterResponse.java        # Registration output DTO
│   ├── entity/
│   │   ├── Barbershop.java              # Barbershop JPA entity
│   │   ├── User.java                    # User JPA entity
│   │   └── UserRole.java                # User role enum
│   ├── repository/
│   │   ├── BarbershopRepository.java    # Barbershop data access
│   │   └── UserRepository.java          # User data access
│   └── service/
│       ├── AuthService.java             # Registration business logic
│       ├── BarbershopService.java       # Barbershop business logic
│       └── UserService.java             # User business logic
├── src/main/resources/
│   ├── application.properties           # App configuration
│   ├── docker-compose.yml               # PostgreSQL container
│   └── db/migration/                    # Flyway migrations (future)
└── build.gradle                         # Dependencies & build config
```

---

## Validation Architecture

### Jakarta Bean Validation (JSR 380)

**Technology Stack:**
- **Jakarta Bean Validation 3.0** - Java standard for validation
- **Hibernate Validator** - Implementation (auto-included with Spring Boot)
- **Spring Validation** - Integration layer with `@Valid` annotation

### How It Works

**1. Define Validation Rules in DTO:**

```java
// src/main/java/com/trimminflow/demo/dto/RegisterRequest.java

public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$",
        message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters")
    private String firstName;

    // ... more fields with validation annotations
}
```

**2. Trigger Validation in Controller:**

```java
// src/main/java/com/trimminflow/demo/controller/AuthController.java

@PostMapping("/register")
public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    // @Valid triggers automatic validation before method execution
    // If validation fails, MethodArgumentNotValidException is thrown
    RegisterResponse response = authService.registerBarbershopOwner(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**3. Handle Validation Errors Globally:**

```java
// src/main/java/com/trimminflow/demo/config/GlobalExceptionHandler.java

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        // Extract field-specific error messages
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        // Return structured error response
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("fieldErrors", fieldErrors);
        response.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
```

### Validation Error Response Format

**Request:**
```json
POST /api/v1/auth/register
{
  "email": "invalid-email",
  "password": "weak",
  "firstName": "J",
  "lastName": "Doe",
  "barbershopName": "My Shop"
}
```

**Response (400 Bad Request):**
```json
{
  "error": "Validation failed",
  "status": 400,
  "fieldErrors": {
    "email": "Please provide a valid email address",
    "password": "Password must be at least 8 characters",
    "firstName": "First name must be between 2 and 50 characters"
  }
}
```

### Validation Rules Reference

| Field | Constraints | Annotations Used |
|-------|-------------|------------------|
| **email** | Required, valid email format | `@NotBlank`, `@Email` |
| **password** | 8+ chars, uppercase, lowercase, number, special char | `@NotBlank`, `@Size(min=8)`, `@Pattern` |
| **firstName** | 2-50 chars, letters/spaces/hyphens/apostrophes only | `@NotBlank`, `@Size(min=2, max=50)`, `@Pattern` |
| **lastName** | 2-50 chars, letters/spaces/hyphens/apostrophes only | `@NotBlank`, `@Size(min=2, max=50)`, `@Pattern` |
| **barbershopName** | 3-100 chars | `@NotBlank`, `@Size(min=3, max=100)` |
| **phone** | Optional, international phone format | `@Pattern` (allows empty) |
| **address** | Optional, max 200 chars | `@Size(max=200)` |

### OWASP Password Requirements

Password regex breakdown:
```regex
^(?=.*[a-z])        # At least one lowercase letter
 (?=.*[A-Z])        # At least one uppercase letter
 (?=.*\d)           # At least one digit
 (?=.*[!@#$%^&*(),.?":{}|<>])  # At least one special character
 [A-Za-z\d!@#$%^&*(),.?":{}|<>]{8,}$  # Only allowed chars, min 8 length
```

Allowed special characters: `!@#$%^&*(),.?":{}|<>`

---

## API Endpoints

### Authentication

#### Register Barbershop Owner
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "barbershopName": "Classic Cuts Barbershop",
  "email": "owner@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+351912345678",
  "address": "123 Main St, Lisbon"
}
```

**Success Response (201 Created):**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "barbershopId": "987fcdeb-51a2-43f7-8d9e-1234567890ab",
  "email": "owner@example.com",
  "message": "Barbershop registered successfully"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Validation failed",
  "status": 400,
  "fieldErrors": {
    "password": "Password must contain at least one uppercase letter"
  }
}
```

### Barbershops

#### Get All Barbershops
```http
GET /api/v1/barbershops
```

**Response (200 OK):**
```json
[
  {
    "id": "uuid",
    "name": "Classic Cuts",
    "email": "contact@classiccuts.com",
    "phone": "+351912345678",
    "address": "123 Main St",
    "description": "Premium barbershop",
    "timezone": "Europe/Lisbon",
    "businessHours": "{\"monday\": \"09:00-18:00\"}",
    "qrCodeUrl": null,
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  }
]
```

#### Get Barbershop by ID
```http
GET /api/v1/barbershops/{id}
```

#### Create Barbershop
```http
POST /api/v1/barbershops
Content-Type: application/json

{
  "name": "New Barbershop",
  "email": "new@barbershop.com",
  "phone": "+351912345678"
}
```

#### Update Barbershop
```http
PUT /api/v1/barbershops/{id}
Content-Type: application/json
```

#### Delete Barbershop
```http
DELETE /api/v1/barbershops/{id}
```

### Users

#### Get All Users
```http
GET /api/users
```

#### Get User by ID
```http
GET /api/users/{id}
```

#### Get User by Email
```http
GET /api/users/email/{email}
```

**Response (200 OK):**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "ADMIN",
  "barbershop": {
    "id": "uuid",
    "name": "Classic Cuts"
  },
  "lastLogin": "2025-01-15T14:20:00",
  "createdAt": "2025-01-10T09:00:00",
  "updatedAt": "2025-01-15T14:20:00"
}
```

**Note:** Password hash is never returned in API responses.

---

## Database Schema

### Tables

#### `barbershop`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PRIMARY KEY |
| `name` | VARCHAR | NOT NULL |
| `email` | VARCHAR | UNIQUE, NOT NULL |
| `phone` | VARCHAR | |
| `address` | VARCHAR | |
| `description` | TEXT | |
| `timezone` | VARCHAR(50) | |
| `business_hours` | JSONB | |
| `qr_code_url` | VARCHAR(500) | |
| `created_at` | TIMESTAMP | AUTO |
| `updated_at` | TIMESTAMP | AUTO |

#### `users`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PRIMARY KEY |
| `email` | VARCHAR | UNIQUE, NOT NULL |
| `first_name` | VARCHAR | NOT NULL |
| `last_name` | VARCHAR | NOT NULL |
| `password_hash` | VARCHAR | NOT NULL |
| `barbershop_id` | UUID | FK → barbershop(id), NOT NULL |
| `role` | VARCHAR (ENUM) | NOT NULL |
| `last_login` | TIMESTAMP | |
| `created_at` | TIMESTAMP | AUTO |
| `updated_at` | TIMESTAMP | AUTO |

**User Roles:**
- `ADMIN` - Barbershop owner, full access
- `BARBER` - Barber employee
- `RECEPTIONIST` - Front desk staff

### Relationships

```
barbershop (1) ─── (N) users
```

---

## Security

### Password Security

**BCrypt Hashing:**
- Configured in `SecurityConfig.java`
- Strength: 10 (2^10 = 1024 rounds)
- Salt automatically generated per password
- Example hash: `$2a$10$N9qo8uLOickgx2ZMRZoMye...`

**Password Encoding in Service:**
```java
// src/main/java/com/trimminflow/demo/service/AuthService.java

@Transactional
public RegisterResponse registerBarbershopOwner(RegisterRequest request) {
    User owner = new User();
    owner.setPassword(passwordEncoder.encode(request.getPassword()));
    // ... save to database
}
```

**Never:**
- Store passwords in plain text
- Log passwords
- Return password hashes in API responses
- Use reversible encryption (hashing is one-way)

### CORS Configuration

Configured in `application.properties`:
```properties
cors.allowed.origins=http://localhost:3000,http://localhost:5173
```

Allows Next.js (3000) and Vite (5173) development servers.

**Production:** Update to your production domain.

### JWT Configuration (Ready, Not Yet Implemented)

```properties
jwt.secret=TrimminFlowSecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong
jwt.expiration=86400000  # 24 hours in milliseconds
```

**Dependencies installed:**
- `jjwt-api` - JWT API
- `jjwt-impl` - JWT implementation
- `jjwt-jackson` - JSON serialization

**TODO:** Implement JWT service, login endpoint, and authentication filter.

---

## Configuration

### Environment Variables

**Required:**
- `SPRING_DATASOURCE_URL` - PostgreSQL connection string (default: `jdbc:postgresql://localhost:5432/trimminflow`)
- `SPRING_DATASOURCE_USERNAME` - Database user (default: `trimminflow_user`)
- `SPRING_DATASOURCE_PASSWORD` - Database password

**Optional (for production):**
- `JWT_SECRET` - JWT signing key (256-bit minimum)
- `STRIPE_SECRET_KEY` - Stripe API key
- `RESEND_API_KEY` - Resend email API key
- `CORS_ALLOWED_ORIGINS` - Comma-separated allowed origins

### application.properties

**Database:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/trimminflow
spring.datasource.username=trimminflow_user
spring.datasource.password=mx269518
spring.jpa.hibernate.ddl-auto=update  # Dev: auto-schema; Prod: validate
spring.jpa.show-sql=true
```

**Flyway (Disabled):**
```properties
spring.flyway.enabled=false
# Future: Enable for production migrations
```

**JWT:**
```properties
jwt.secret=your-256-bit-secret-key
jwt.expiration=86400000
```

**External APIs:**
```properties
stripe.api.key=${STRIPE_SECRET_KEY:sk_test_default}
resend.api.key=${RESEND_API_KEY:default}
```

---

## Running the Application

### Prerequisites

1. **Java 17+** installed
2. **PostgreSQL** running (Docker Compose or local)
3. **Gradle** 8.x (or use wrapper `./gradlew`)

### Start Database (Docker)

```bash
cd demo/src/main/resources
docker-compose up -d
```

**docker-compose.yml:**
```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: trimminflow
      POSTGRES_USER: trimminflow_user
      POSTGRES_PASSWORD: mx269518
    ports:
      - "5432:5432"
```

### Run Application

```bash
cd demo
./gradlew bootRun
```

**Application starts on:** `http://localhost:8080`

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

### Development Mode Features

- **Hot reload** with Spring DevTools
- **SQL logging** (`spring.jpa.show-sql=true`)
- **Auto schema updates** (`hibernate.ddl-auto=update`)
- **LiveReload** on port 35729

---

## Common Validation Patterns

### Required Field
```java
@NotBlank(message = "Field is required")
private String fieldName;
```

### Email Validation
```java
@Email(message = "Invalid email address")
@NotBlank
private String email;
```

### String Length
```java
@Size(min = 2, max = 50, message = "Must be 2-50 characters")
private String name;
```

### Regex Pattern
```java
@Pattern(regexp = "^[A-Z0-9]+$", message = "Only uppercase letters and numbers")
private String code;
```

### Optional Field with Validation
```java
@Pattern(
    regexp = "^$|^[\\+]?[(]?[0-9]{1,3}...",  // Note: ^$ allows empty string
    message = "Invalid phone number"
)
private String phone;  // No @NotBlank = optional
```

### Multiple Constraints
```java
@NotBlank(message = "Password is required")
@Size(min = 8, message = "Must be at least 8 characters")
@Pattern(regexp = "...", message = "Must contain uppercase, lowercase, number, special char")
private String password;
```

---

## Testing the API

### Using cURL

**Register Barbershop:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "barbershopName": "Test Shop",
    "email": "test@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+351912345678"
  }'
```

**Get All Barbershops:**
```bash
curl http://localhost:8080/api/v1/barbershops
```

### Using Swagger UI

1. Navigate to `http://localhost:8080/swagger-ui.html`
2. Expand endpoint (e.g., `POST /api/v1/auth/register`)
3. Click "Try it out"
4. Fill in request body
5. Click "Execute"
6. View response

### Using Postman

1. Import collection from Swagger JSON: `http://localhost:8080/v3/api-docs`
2. Test endpoints with pre-filled schemas
3. Save test cases for regression testing

---

## Next Steps

### Backend Features to Implement

1. **Authentication**
   - [ ] JWT token generation on login
   - [ ] Login endpoint (`POST /api/v1/auth/login`)
   - [ ] Refresh token endpoint
   - [ ] JWT authentication filter
   - [ ] Role-based access control (RBAC)
   - [ ] Logout / token blacklist

2. **User Management**
   - [ ] Add barber/receptionist users (RBAC)
   - [ ] User profile update endpoint
   - [ ] Password reset flow
   - [ ] Email verification

3. **Barber Management**
   - [ ] Barber entity & CRUD endpoints
   - [ ] Barber availability/schedule
   - [ ] Assign barbers to barbershops

4. **Service Management**
   - [ ] Service entity (name, duration, price)
   - [ ] CRUD endpoints
   - [ ] Service categories

5. **Appointment System**
   - [ ] Appointment entity
   - [ ] Create appointment endpoint
   - [ ] List appointments (filter by date, barber, status)
   - [ ] Update/cancel appointment
   - [ ] Appointment status workflow (scheduled → in_progress → completed → cancelled)

6. **Customer Management**
   - [ ] Customer entity
   - [ ] Customer registration (simplified, no auth)
   - [ ] Customer appointment history

7. **QR Code Generation**
   - [ ] Generate QR code on barbershop creation
   - [ ] Store QR code URL
   - [ ] Endpoint to retrieve QR code

8. **Payment Integration**
   - [ ] Stripe payment intent creation
   - [ ] Webhook handling
   - [ ] Payment entity & history

9. **Email Notifications**
   - [ ] Resend integration
   - [ ] Appointment confirmation email
   - [ ] Appointment reminder email
   - [ ] Password reset email

10. **Database Migrations**
    - [ ] Enable Flyway
    - [ ] Create initial migration from current schema
    - [ ] Version control schema changes

11. **Testing**
    - [ ] Unit tests for services
    - [ ] Integration tests for controllers
    - [ ] Repository tests
    - [ ] Test coverage > 80%

12. **Production Readiness**
    - [ ] Change `hibernate.ddl-auto` to `validate`
    - [ ] Enable Flyway migrations
    - [ ] Externalize secrets to environment variables
    - [ ] Add logging (SLF4J + Logback)
    - [ ] Add health check endpoints
    - [ ] Add metrics (Actuator)
    - [ ] Rate limiting
    - [ ] API versioning strategy

### Frontend Integration

1. **Connect remaining endpoints**
   - [ ] Login page → `POST /api/v1/auth/login`
   - [ ] Dashboard → `GET /api/v1/appointments`
   - [ ] Calendar → `GET /api/v1/appointments?date=...`
   - [ ] Booking flow → `POST /api/v1/appointments`

2. **Authentication Flow**
   - [ ] Store JWT in localStorage/httpOnly cookie
   - [ ] Add Authorization header to API requests
   - [ ] Implement auto-refresh token
   - [ ] Redirect to login on 401 Unauthorized

3. **Protected Routes**
   - [ ] Check JWT validity before rendering protected pages
   - [ ] Role-based component rendering (admin vs barber)

---

## Key Design Decisions

### Why DTOs Instead of Direct Entity Validation?

**Pros:**
- Decouples API contract from database schema
- Prevents over-posting (clients can't set `id`, `createdAt`, etc.)
- Different validation rules for create vs. update
- API evolution without breaking database

**Example:**
```java
// DTO (API layer) - has validation
@NotBlank(message = "Email is required")
private String email;

// Entity (database layer) - no detailed validation messages
@Column(nullable = false, unique = true)
private String email;
```

### Why @Transactional on Registration?

Ensures atomicity:
```java
@Transactional
public RegisterResponse registerBarbershopOwner(RegisterRequest request) {
    // Step 1: Save barbershop
    Barbershop savedBarbershop = barbershopRepository.save(barbershop);

    // Step 2: Save owner user
    User savedUser = userRepository.save(owner);

    // If Step 2 fails, Step 1 is rolled back automatically
}
```

Without `@Transactional`, you could end up with orphaned barbershops.

### Why UUID Instead of Auto-Increment IDs?

**Pros:**
- Globally unique (safe for distributed systems)
- Can't guess other records' IDs
- Safe to generate client-side
- Easier for data merging

**Cons:**
- Larger storage (16 bytes vs 4 bytes for INT)
- Slightly slower indexing

**Verdict:** Security and scalability benefits outweigh storage cost.

### Why BCrypt Strength 10?

- **Too low (< 8):** Fast, but vulnerable to brute force
- **10:** Industry standard, ~100ms per hash (good UX, secure)
- **Too high (> 12):** Slow login experience, DDoS vulnerability

**Example hash time:**
```
Strength 10: ~100ms
Strength 12: ~400ms
Strength 14: ~1.6s
```

---

## Resources

### Documentation
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Jakarta Bean Validation](https://beanvalidation.org/)
- [Hibernate Validator Docs](https://docs.jboss.org/hibernate/validator/8.0/reference/en-US/html_single/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Swagger/OpenAPI](https://springdoc.org/)

### Tools
- [Postman](https://www.postman.com/) - API testing
- [DBeaver](https://dbeaver.io/) - PostgreSQL client
- [Swagger Editor](https://editor.swagger.io/) - OpenAPI schema design

### Libraries
- [jjwt Documentation](https://github.com/jwtk/jjwt) - JWT implementation
- [Stripe Java SDK](https://stripe.com/docs/api?lang=java)
- [Resend Java SDK](https://resend.com/docs/send-with-java)
- [ZXing (QR Codes)](https://github.com/zxing/zxing)

---

## Troubleshooting

### Port 8080 Already in Use
```bash
# Find process using port 8080
lsof -i :8080
# Kill process
kill -9 <PID>
```

### Database Connection Refused
```bash
# Check if PostgreSQL is running
docker ps
# Start database
cd demo/src/main/resources && docker-compose up -d
```

### Validation Not Working
1. Ensure `spring-boot-starter-validation` dependency is in `build.gradle`
2. Check `@Valid` annotation is present in controller
3. Verify `GlobalExceptionHandler` is annotated with `@RestControllerAdvice`
4. Check logs for validation errors

### BCrypt Password Encoding Fails
Ensure `SecurityConfig` is present with `PasswordEncoder` bean:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### CORS Errors
Update `application.properties`:
```properties
cors.allowed.origins=http://localhost:3000,http://your-frontend-domain.com
```

---

## Contributors

**Backend Developer:** Maxi G
**Project:** TrimminFlow - Barbershop Management SaaS
**License:** Proprietary

---

## Version History

- **v0.1.0** (2025-01-19) - Initial backend implementation
  - Barbershop & owner registration
  - Jakarta Bean Validation
  - BCrypt password hashing
  - PostgreSQL integration
  - Swagger documentation

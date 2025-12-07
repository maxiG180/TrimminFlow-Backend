# Security & OWASP Top 10 Evidence

This document details the security enhancements implemented in the TrimminFlow application, specifically addressing the **OWASP Top 10 (2021)** vulnerabilities. The primary focus was on securing the backend API against unauthorized access and data manipulation.

## 1. Addressed Vulnerabilities

### A. A01:2021 - Broken Access Control (CRITICAL FIX)
**Previous State:**
The application relied on a client-provided HTTP header `X-Barbershop-Id` to identify which barbershop's data to access.
*   **Risk**: A malicious user could modify this header (IDOR - Insecure Direct Object Reference) to access or manipulate data belonging to other barbershops.

**Implemented Solution:**
We removed reliance on the `X-Barbershop-Id` header for protected resources and implemented **Context-Aware Authorization**.
1.  **Authentication Context**: We introduced a `getAuthenticatedUser()` helper method in all controllers (`BarberController`, `ServiceController`, `AppointmentController`, etc.).
2.  **Derived Identity**: The `barbershopId` is now derived directly from the authenticated `User` entity stored in the Spring Security context (JWT token).
3.  **Ownership Verification**:
    *   For operations requiring an ID (e.g., `updateBarbershop(id)`), we explicitly check if the authenticated user's ID matches the target ID.
    *   `if (!user.getBarbershop().getId().equals(id)) { throw new ForbiddenException(); }`

**Affected Controllers:**
*   `BarberController`: Users can only create/manage barbers for their own shop.
*   `ServiceController`: Services are strictly scoped to the user's barbershop.
*   `AppointmentController`: Appointments are fetched/created based on the user's context.
*   `UserController`: Users can only view/update their own profile (`/me` endpoint or ID check).

### B. A02:2021 - Cryptographic Failures
*   **Password Storage**: All user passwords are hashed using **BCrypt** before storage in the database.
*   **Transport Security**: The application is designed to run over HTTPS (SSL/TLS) in production.
*   **Secrets Management**: API keys (e.g., Cloudinary, Database credentials) are loaded from environment variables or `application.properties`, not hardcoded in source files.

### C. A03:2021 - Injection
*   **SQL Injection Prevention**: The application uses **Spring Data JPA** and **Hibernate**. All database queries use parameterized queries or the Criteria API by default, effectively neutralizing SQL injection attacks.
*   **Input Validation**: We use `@Valid` and `Jakarta Validation` constraints (e.g., `@Email`, `@NotBlank`) on DTOs to ensure input data conforms to expected formats before processing.

### D. A07:2021 - Identification and Authentication Failures
*   **JWT Authentication**: Implemented a stateless JWT (JSON Web Token) authentication mechanism.
*   **Security Filter**: `JwtAuthenticationFilter` intercepts requests to validate tokens before they reach the controllers.
*   **Strict Access**: Public endpoints are explicitly defined (e.g., Registration, Login), while all other endpoints default to requiring authentication.

## 2. Security Architecture Overview

### Authentication Flow
1.  **Login**: User sends credentials to `/api/v1/auth/login`.
2.  **Token Issue**: Server validates credentials and issues a signed JWT.
3.  **Request**: Client sends JWT in `Authorization: Bearer <token>` header.
4.  **Validation**: `JwtAuthenticationFilter` validates the signature and expiration.
5.  **Context**: Valid user details are loaded into `SecurityContextHolder`.

### Authorization Flow (Example: `ServiceController`)
```java
@GetMapping
public ResponseEntity<List<ServiceResponse>> getAllServices() {
    // 1. Retrieve User from Secure Context (NOT from Header)
    User user = getAuthenticatedUser();
    
    // 2. Derive Scope
    UUID barbershopId = user.getBarbershop().getId();
    
    // 3. Fetch Data Scoped to User
    return serviceManagementService.getAllServices(barbershopId);
}
```

## 3. Conclusion
The TrimminFlow application has moved from a "trust the client" model to a "zero trust" model regarding data scope. By enforcing authorization at the controller level based on the authenticated identity, we have effectively mitigated the risk of horizontal privilege escalation and data leakage between tenants (barbershops).

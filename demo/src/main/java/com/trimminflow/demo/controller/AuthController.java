package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.LoginRequest;
import com.trimminflow.demo.dto.LoginResponse;
import com.trimminflow.demo.dto.RegisterRequest;
import com.trimminflow.demo.dto.RegisterResponse;
import com.trimminflow.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and registration APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new barbershop owner", description = "Register a new barbershop with an owner account. Creates both barbershop and owner user.")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.registerBarbershopOwner(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new RegisterResponse(null, null, null, e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate a user with email and password. Sets JWT in httpOnly cookie for security.")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.login(request);

            // Create httpOnly cookie with JWT token using ResponseCookie
            // For CORS (localhost:3000 -> localhost:8080), we need SameSite=None and
            // Secure=true
            org.springframework.http.ResponseCookie jwtCookie = org.springframework.http.ResponseCookie
                    .from("accessToken", loginResponse.getAccessToken())
                    .httpOnly(true)
                    .secure(true) // ✅ Required for SameSite=None, even on localhost
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("None") // ✅ Required for cross-site/cross-port cookies
                    .build();

            response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, jwtCookie.toString());

            // Remove token from response body for security
            loginResponse.setAccessToken(null);

            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Clears the authentication cookie")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Clear the cookie by setting maxAge to 0
        org.springframework.http.ResponseCookie jwtCookie = org.springframework.http.ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, jwtCookie.toString());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Validates the JWT token from httpOnly cookie. Returns 200 if valid, 401 if invalid/expired.")
    public ResponseEntity<Void> validateToken() {
        // If this endpoint is reached, the JWT filter already validated the token
        // and set the authentication in SecurityContext
        // If the token was invalid, Spring Security would have returned 401
        // automatically
        return ResponseEntity.ok().build();
    }
}
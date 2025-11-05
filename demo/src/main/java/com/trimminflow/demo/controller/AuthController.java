package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.LoginRequest;
import com.trimminflow.demo.dto.LoginResponse;
import com.trimminflow.demo.dto.RegisterRequest;
import com.trimminflow.demo.dto.RegisterResponse;
import com.trimminflow.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(
        summary = "Register new barbershop owner",
        description = "Register a new barbershop with an owner account. Creates both barbershop and owner user."
    )
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.registerBarbershopOwner(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new RegisterResponse(null, null, null, e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login user",
        description = "Authenticate a user with email and password. Returns JWT access token."
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
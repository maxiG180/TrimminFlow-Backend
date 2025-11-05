package com.trimminflow.demo.dto;

import java.util.UUID;

/**
 * Login Response DTO
 *
 * Returned when a user successfully logs in
 * Contains the JWT access token and user information
 */
public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private UUID barbershopId;
    private String message;

    public LoginResponse() {}

    public LoginResponse(String accessToken, UUID userId, String email,
                        String firstName, String lastName, String role,
                        UUID barbershopId, String message) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.barbershopId = barbershopId;
        this.message = message;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public UUID getBarbershopId() { return barbershopId; }
    public void setBarbershopId(UUID barbershopId) { this.barbershopId = barbershopId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

package com.trimminflow.demo.dto;

import java.util.UUID;

public class RegisterResponse {

    private UUID userId;
    private UUID barbershopId;
    private String email;
    private String message;

    public RegisterResponse() {}

    public RegisterResponse(UUID userId, UUID barbershopId, String email, String message) {
        this.userId = userId;
        this.barbershopId = barbershopId;
        this.email = email;
        this.message = message;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getBarbershopId() { return barbershopId; }
    public void setBarbershopId(UUID barbershopId) { this.barbershopId = barbershopId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

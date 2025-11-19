package com.trimminflow.demo.dto;

import com.trimminflow.demo.entity.Barber;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BarberResponse DTO
 *
 * Used when returning barber data to the frontend
 */
public class BarberResponse {

    private UUID id;
    private UUID barbershopId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String bio;
    private String profileImageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public BarberResponse() {}

    public BarberResponse(Barber barber) {
        this.id = barber.getId();
        this.barbershopId = barber.getBarbershop().getId();
        this.firstName = barber.getFirstName();
        this.lastName = barber.getLastName();
        this.email = barber.getEmail();
        this.phone = barber.getPhone();
        this.bio = barber.getBio();
        this.profileImageUrl = barber.getProfileImageUrl();
        this.isActive = barber.getIsActive();
        this.createdAt = barber.getCreatedAt();
        this.updatedAt = barber.getUpdatedAt();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBarbershopId() { return barbershopId; }
    public void setBarbershopId(UUID barbershopId) { this.barbershopId = barbershopId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper method
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

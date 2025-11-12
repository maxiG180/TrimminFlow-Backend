package com.trimminflow.demo.dto;

import jakarta.validation.constraints.Email;

/**
 * UpdateBarberRequest DTO
 *
 * Used when updating an existing barber
 * All fields are optional - only provided fields will be updated
 */
public class UpdateBarberRequest {

    private String firstName;

    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String bio;

    private Boolean isActive;

    // Constructors
    public UpdateBarberRequest() {}

    // Getters and Setters
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

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

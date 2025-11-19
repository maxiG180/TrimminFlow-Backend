package com.trimminflow.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * UpdateBarberRequest DTO
 *
 * Used when updating an existing barber
 * All fields are optional - only provided fields will be updated
 */
public class UpdateBarberRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^$|^[\\+]?[(]?[0-9]{1,3}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,4}[-\\s\\.]?[0-9]{1,9}$",
             message = "Please provide a valid phone number")
    private String phone;

    @Size(max = 500, message = "Bio must be less than 500 characters")
    private String bio;

    private Boolean isActive;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String profileImageUrl;

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

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}

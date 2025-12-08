package com.trimminflow.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * UpdateBarberRequest DTO
 *
 * Used when updating an existing barber
 * All fields are optional - only provided fields will be updated
 */
public class UpdateBarberRequest extends BaseBarberRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    private Boolean isActive;
    private Boolean removeProfileImage;

    public UpdateBarberRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getRemoveProfileImage() {
        return removeProfileImage;
    }

    public void setRemoveProfileImage(Boolean removeProfileImage) {
        this.removeProfileImage = removeProfileImage;
    }
}

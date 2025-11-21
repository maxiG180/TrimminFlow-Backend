package com.trimminflow.demo.dto;

/**
 * UpdateServiceRequest DTO
 *
 * Used when updating an existing service
 * All fields are optional - only provided fields will be updated
 */
public class UpdateServiceRequest extends BaseServiceRequest {

    private Boolean isActive;

    // Constructors
    public UpdateServiceRequest() {
    }

    // Getters and Setters
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

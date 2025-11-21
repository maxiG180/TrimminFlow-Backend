package com.trimminflow.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * CreateServiceRequest DTO
 *
 * Used when creating a new service
 */
public class CreateServiceRequest extends BaseServiceRequest {

    // Constructors
    public CreateServiceRequest() {
    }

    public CreateServiceRequest(String name, String description, BigDecimal price, Integer durationMinutes) {
        this.setName(name);
        this.setDescription(description);
        this.setPrice(price);
        this.setDurationMinutes(durationMinutes);
    }

    // Validation overrides
    @Override
    @NotBlank(message = "Service name is required")
    public String getName() {
        return super.getName();
    }

    @Override
    @NotNull(message = "Price is required")
    public BigDecimal getPrice() {
        return super.getPrice();
    }

    @Override
    @NotNull(message = "Duration is required")
    public Integer getDurationMinutes() {
        return super.getDurationMinutes();
    }
}

package com.trimminflow.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * BaseServiceRequest
 *
 * Base class for service requests containing common fields.
 */
public class BaseServiceRequest {

    private String name;
    private String description;

    @DecimalMin(value = "0.0", message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}

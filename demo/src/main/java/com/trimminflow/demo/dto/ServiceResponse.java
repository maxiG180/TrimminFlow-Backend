package com.trimminflow.demo.dto;

import com.trimminflow.demo.entity.Service;

import java.math.BigDecimal;

/**
 * ServiceResponse DTO
 *
 * Returned when fetching service information
 * Contains all service details without exposing internal entity structure
 */
public class ServiceResponse extends BaseResponse {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;

    public ServiceResponse() {
    }

    public ServiceResponse(Service service) {
        this.setId(service.getId());
        this.setBarbershopId(service.getBarbershop().getId());
        this.name = service.getName();
        this.description = service.getDescription();
        this.price = service.getPrice();
        this.durationMinutes = service.getDurationMinutes();
        this.setIsActive(service.getIsActive());
        this.setCreatedAt(service.getCreatedAt());
        this.setUpdatedAt(service.getUpdatedAt());
    }

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

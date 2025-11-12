package com.trimminflow.demo.dto;

import com.trimminflow.demo.entity.BusinessHours;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class BusinessHoursResponse {
    private UUID id;
    private UUID barbershopId;
    private String dayOfWeek;
    private Boolean isOpen;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BusinessHoursResponse() {}

    public BusinessHoursResponse(BusinessHours businessHours) {
        this.id = businessHours.getId();
        this.barbershopId = businessHours.getBarbershop().getId();
        this.dayOfWeek = businessHours.getDayOfWeek();
        this.isOpen = businessHours.getIsOpen();
        this.openTime = businessHours.getOpenTime();
        this.closeTime = businessHours.getCloseTime();
        this.createdAt = businessHours.getCreatedAt();
        this.updatedAt = businessHours.getUpdatedAt();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getBarbershopId() { return barbershopId; }
    public void setBarbershopId(UUID barbershopId) { this.barbershopId = barbershopId; }
    
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public Boolean getIsOpen() { return isOpen; }
    public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }
    
    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }
    
    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

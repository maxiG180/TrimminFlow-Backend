package com.trimminflow.demo.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class SetBusinessHoursRequest {
    @NotNull(message = "Day of week is required")
    private String dayOfWeek;
    
    @NotNull(message = "isOpen is required")
    private Boolean isOpen;
    
    private LocalTime openTime;
    private LocalTime closeTime;

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public Boolean getIsOpen() { return isOpen; }
    public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }
    
    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }
    
    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
}

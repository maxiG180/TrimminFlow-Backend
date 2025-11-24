package com.trimminflow.demo.dto;

import com.trimminflow.demo.entity.DayOfWeek;
import com.trimminflow.demo.validation.ValidBusinessHours;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

@ValidBusinessHours
public class SetBusinessHoursRequest {
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "isOpen is required")
    private Boolean isOpen;

    private LocalTime openTime;
    private LocalTime closeTime;

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public Boolean getIsOpen() { return isOpen; }
    public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }
    
    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }
    
    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
}

package com.trimminflow.demo.dto;

import com.trimminflow.demo.entity.AppointmentStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for updating an existing appointment
 * All fields are optional - only provided fields will be updated
 */
public class UpdateAppointmentRequest {

    private LocalDateTime appointmentDateTime;

    private UUID serviceId;

    private AppointmentStatus status;

    @Size(max = 500, message = "Notes must be less than 500 characters")
    private String notes;

    @Pattern(regexp = "^$|^[\\+]?[(]?[0-9]{1,3}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,4}[-\\s\\.]?[0-9]{1,9}$", message = "Please provide a valid phone number")
    private String customerPhone;

    public UpdateAppointmentRequest() {
    }

    public UpdateAppointmentRequest(LocalDateTime appointmentDateTime, UUID serviceId,
            AppointmentStatus status, String notes, String customerPhone) {
        this.appointmentDateTime = appointmentDateTime;
        this.serviceId = serviceId;
        this.status = status;
        this.notes = notes;
        this.customerPhone = customerPhone;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
}

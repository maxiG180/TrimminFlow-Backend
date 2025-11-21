package com.trimminflow.demo.dto;

import com.trimminflow.demo.entity.Appointment;
import com.trimminflow.demo.entity.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Appointment
 * Contains all appointment details with nested barber and service info
 */
public class AppointmentResponse {

    private UUID id;
    private BarbershopInfo barbershop;
    private BarberInfo barber;
    private ServiceInfo service;
    private LocalDateTime appointmentDateTime;
    private LocalDateTime endDateTime;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested DTOs for related entities
    public static class BarbershopInfo {
        private UUID id;
        private String name;

        public BarbershopInfo(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class BarberInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String profileImageUrl;

        public BarberInfo(UUID id, String firstName, String lastName, String profileImageUrl) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.profileImageUrl = profileImageUrl;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
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

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public void setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    public static class ServiceInfo {
        private UUID id;
        private String name;
        private Double price;
        private Integer durationMinutes;

        public ServiceInfo(UUID id, String name, Double price, Integer durationMinutes) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.durationMinutes = durationMinutes;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Integer getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
        }
    }

    // Factory method to create from entity
    public static AppointmentResponse fromEntity(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());

        response.setBarbershop(new BarbershopInfo(
                appointment.getBarbershop().getId(),
                appointment.getBarbershop().getName()));

        response.setBarber(new BarberInfo(
                appointment.getBarber().getId(),
                appointment.getBarber().getFirstName(),
                appointment.getBarber().getLastName(),
                appointment.getBarber().getProfileImageUrl()));

        response.setService(new ServiceInfo(
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getService().getPrice().doubleValue(),
                appointment.getService().getDurationMinutes()));

        response.setAppointmentDateTime(appointment.getAppointmentDateTime());
        response.setEndDateTime(appointment.getEndDateTime());
        response.setCustomerName(appointment.getCustomerName());
        response.setCustomerEmail(appointment.getCustomerEmail());
        response.setCustomerPhone(appointment.getCustomerPhone());
        response.setStatus(appointment.getStatus());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());

        return response;
    }

    // Constructors
    public AppointmentResponse() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BarbershopInfo getBarbershop() {
        return barbershop;
    }

    public void setBarbershop(BarbershopInfo barbershop) {
        this.barbershop = barbershop;
    }

    public BarberInfo getBarber() {
        return barber;
    }

    public void setBarber(BarberInfo barber) {
        this.barber = barber;
    }

    public ServiceInfo getService() {
        return service;
    }

    public void setService(ServiceInfo service) {
        this.service = service;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

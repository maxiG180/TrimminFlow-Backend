package com.trimminflow.demo.entity;

/**
 * Appointment Status Enum
 *
 * Represents the lifecycle status of an appointment
 */
public enum AppointmentStatus {
    /**
     * Appointment has been created but not yet confirmed
     */
    PENDING,

    /**
     * Appointment has been confirmed by the barbershop
     */
    CONFIRMED,

    /**
     * Appointment was cancelled (by customer or barbershop)
     */
    CANCELLED,

    /**
     * Appointment was completed successfully
     */
    COMPLETED,

    /**
     * Customer did not show up for the appointment
     */
    NO_SHOW
}

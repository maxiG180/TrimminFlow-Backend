package com.trimminflow.demo.repository;

import com.trimminflow.demo.entity.Appointment;
import com.trimminflow.demo.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Appointment entity
 *
 * Provides database access methods for appointments including
 * conflict detection and availability queries
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    /**
     * Find all appointments for a barbershop, excluding a specific status
     */
    Page<Appointment> findByBarbershopIdAndStatusNot(
            UUID barbershopId,
            AppointmentStatus status,
            Pageable pageable);

    /**
     * Find all appointments for a barbershop within a date range
     */
    @Query("SELECT a FROM Appointment a WHERE a.barbershop.id = :barbershopId " +
            "AND a.appointmentDateTime >= :startDate " +
            "AND a.appointmentDateTime < :endDate " +
            "ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findByBarbershopIdAndDateRange(
            @Param("barbershopId") UUID barbershopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find all appointments for a specific barber within a date range
     */
    @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId " +
            "AND a.appointmentDateTime >= :startDate " +
            "AND a.appointmentDateTime < :endDate " +
            "AND a.status NOT IN ('CANCELLED') " +
            "ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findByBarberIdAndDateRange(
            @Param("barberId") UUID barberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Check if a barber has any conflicting appointments
     * (appointments that overlap with the given time range)
     */
    @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId " +
            "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
            "AND a.appointmentDateTime < :endTime " +
            "AND a.endDateTime > :startTime")
    List<Appointment> findConflictingAppointments(
            @Param("barberId") UUID barberId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Quick check if barber has any appointments in a time range
     */
    boolean existsByBarberIdAndAppointmentDateTimeBetweenAndStatusNot(
            UUID barberId,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status);

    /**
     * Find appointments by barber and status
     */
    List<Appointment> findByBarberIdAndStatus(UUID barberId, AppointmentStatus status);

    /**
     * Find appointments by barbershop and status
     */
    Page<Appointment> findByBarbershopIdAndStatus(
            UUID barbershopId,
            AppointmentStatus status,
            Pageable pageable);

    /**
     * Count appointments by barber and status
     */
    long countByBarberIdAndStatus(UUID barberId, AppointmentStatus status);

    /**
     * Count appointments by barbershop and status
     */
    long countByBarbershopIdAndStatus(UUID barbershopId, AppointmentStatus status);
}

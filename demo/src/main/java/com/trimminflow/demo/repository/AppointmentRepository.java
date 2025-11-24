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

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

        Page<Appointment> findByBarbershopIdAndStatusNot(
                        UUID barbershopId,
                        AppointmentStatus status,
                        Pageable pageable);

        @Query("SELECT a FROM Appointment a WHERE a.barbershop.id = :barbershopId " +
                        "AND a.appointmentDateTime >= :startDate " +
                        "AND a.appointmentDateTime < :endDate " +
                        "ORDER BY a.appointmentDateTime ASC")
        List<Appointment> findByBarbershopIdAndDateRange(
                        @Param("barbershopId") UUID barbershopId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId " +
                        "AND a.appointmentDateTime >= :startDate " +
                        "AND a.appointmentDateTime < :endDate " +
                        "AND a.status NOT IN ('CANCELLED') " +
                        "ORDER BY a.appointmentDateTime ASC")
        List<Appointment> findByBarberIdAndDateRange(
                        @Param("barberId") UUID barberId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT a FROM Appointment a WHERE a.barber.id = :barberId " +
                        "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
                        "AND a.appointmentDateTime < :endTime " +
                        "AND a.endDateTime > :startTime")
        List<Appointment> findConflictingAppointments(
                        @Param("barberId") UUID barberId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        boolean existsByBarberIdAndAppointmentDateTimeBetweenAndStatusNot(
                        UUID barberId,
                        LocalDateTime start,
                        LocalDateTime end,
                        AppointmentStatus status);

        List<Appointment> findByBarberIdAndStatus(UUID barberId, AppointmentStatus status);

        Page<Appointment> findByBarbershopIdAndStatus(
                        UUID barbershopId,
                        AppointmentStatus status,
                        Pageable pageable);

        long countByBarberIdAndStatus(UUID barberId, AppointmentStatus status);

        long countByBarbershopIdAndStatus(UUID barbershopId, AppointmentStatus status);
}

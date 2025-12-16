package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.AnalyticsResponse;
import com.trimminflow.demo.entity.AppointmentStatus;
import com.trimminflow.demo.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnalyticsService {

    private final AppointmentRepository appointmentRepository;

    public AnalyticsService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public AnalyticsResponse getAnalytics(UUID barbershopId, String period) {
        AnalyticsResponse response = new AnalyticsResponse();

        // Get date range based on period (default to all time)
        LocalDateTime startDate = getStartDate(period);
        LocalDateTime endDate = LocalDateTime.now();

        // Total appointments
        Long total = appointmentRepository.countByBarbershopId(barbershopId);
        response.setTotalAppointments(total);

        // Completed appointments
        Long completed = appointmentRepository.countByBarbershopIdAndStatus(barbershopId, AppointmentStatus.COMPLETED);
        response.setCompletedAppointments(completed);

        // Cancelled appointments
        Long cancelled = appointmentRepository.countByBarbershopIdAndStatus(barbershopId, AppointmentStatus.CANCELLED);
        response.setCancelledAppointments(cancelled);

        // No-show appointments
        Long noShow = appointmentRepository.countByBarbershopIdAndStatus(barbershopId, AppointmentStatus.NO_SHOW);
        response.setNoShowAppointments(noShow);

        // Calculate revenue (only from COMPLETED appointments)
        List<Object[]> completedAppts = appointmentRepository.findCompletedAppointmentsWithPrice(barbershopId);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Object[] row : completedAppts) {
            BigDecimal price = (BigDecimal) row[0];
            if (price != null) {
                totalRevenue = totalRevenue.add(price);
            }
        }
        response.setTotalRevenue(totalRevenue);

        // Average revenue per appointment
        if (completed > 0) {
            BigDecimal avg = totalRevenue.divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP);
            response.setAverageRevenue(avg);
        } else {
            response.setAverageRevenue(BigDecimal.ZERO);
        }

        // Popular services
        List<Object[]> serviceStats = appointmentRepository.findPopularServices(barbershopId);
        List<AnalyticsResponse.ServiceStats> popularServices = new ArrayList<>();
        for (Object[] row : serviceStats) {
            String serviceName = (String) row[0];
            Long count = (Long) row[1];
            BigDecimal revenue = (BigDecimal) row[2];
            popularServices.add(new AnalyticsResponse.ServiceStats(serviceName, count,
                    revenue != null ? revenue : BigDecimal.ZERO));
        }
        response.setPopularServices(popularServices);

        // Barber performance
        List<Object[]> barberStats = appointmentRepository.findBarberPerformance(barbershopId);
        List<AnalyticsResponse.BarberStats> barberPerformance = new ArrayList<>();
        for (Object[] row : barberStats) {
            String barberName = (String) row[0];
            Long count = (Long) row[1];
            BigDecimal revenue = (BigDecimal) row[2];
            barberPerformance.add(
                    new AnalyticsResponse.BarberStats(barberName, count, revenue != null ? revenue : BigDecimal.ZERO));
        }
        response.setBarberPerformance(barberPerformance);

        // Today's appointments
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        Long todayCount = appointmentRepository.countByBarbershopIdAndAppointmentDateTimeBetween(barbershopId,
                todayStart, todayEnd);
        response.setTodayAppointments(todayCount);

        // This week's appointments
        LocalDateTime weekStart = LocalDate.now().atStartOfDay().minusDays(7);
        Long weekCount = appointmentRepository.countByBarbershopIdAndAppointmentDateTimeBetween(barbershopId, weekStart,
                endDate);
        response.setWeekAppointments(weekCount);

        // This month's appointments
        LocalDateTime monthStart = LocalDate.now().atStartOfDay().minusDays(30);
        Long monthCount = appointmentRepository.countByBarbershopIdAndAppointmentDateTimeBetween(barbershopId,
                monthStart, endDate);
        response.setMonthAppointments(monthCount);

        return response;
    }

    private LocalDateTime getStartDate(String period) {
        if (period == null)
            return LocalDateTime.now().minusYears(10); // All time

        return switch (period.toLowerCase()) {
            case "today" -> LocalDate.now().atStartOfDay();
            case "week" -> LocalDateTime.now().minusDays(7);
            case "month" -> LocalDateTime.now().minusDays(30);
            case "year" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusYears(10);
        };
    }
}

package com.trimminflow.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class AnalyticsResponse {

    // Overview Stats
    private Long totalAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Long noShowAppointments;
    private BigDecimal totalRevenue;
    private BigDecimal averageRevenue;

    // Popular Services
    private List<ServiceStats> popularServices;

    // Barber Performance
    private List<BarberStats> barberPerformance;

    // Recent Activity
    private Long todayAppointments;
    private Long weekAppointments;
    private Long monthAppointments;

    public static class ServiceStats {
        private String serviceName;
        private Long bookingCount;
        private BigDecimal revenue;

        public ServiceStats(String serviceName, Long bookingCount, BigDecimal revenue) {
            this.serviceName = serviceName;
            this.bookingCount = bookingCount;
            this.revenue = revenue;
        }

        public String getServiceName() {
            return serviceName;
        }

        public Long getBookingCount() {
            return bookingCount;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }
    }

    public static class BarberStats {
        private String barberName;
        private Long completedAppointments;
        private BigDecimal revenue;

        public BarberStats(String barberName, Long completedAppointments, BigDecimal revenue) {
            this.barberName = barberName;
            this.completedAppointments = completedAppointments;
            this.revenue = revenue;
        }

        public String getBarberName() {
            return barberName;
        }

        public Long getCompletedAppointments() {
            return completedAppointments;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }
    }

    // Getters and Setters
    public Long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(Long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public Long getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(Long completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public Long getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(Long cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public Long getNoShowAppointments() {
        return noShowAppointments;
    }

    public void setNoShowAppointments(Long noShowAppointments) {
        this.noShowAppointments = noShowAppointments;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAverageRevenue() {
        return averageRevenue;
    }

    public void setAverageRevenue(BigDecimal averageRevenue) {
        this.averageRevenue = averageRevenue;
    }

    public List<ServiceStats> getPopularServices() {
        return popularServices;
    }

    public void setPopularServices(List<ServiceStats> popularServices) {
        this.popularServices = popularServices;
    }

    public List<BarberStats> getBarberPerformance() {
        return barberPerformance;
    }

    public void setBarberPerformance(List<BarberStats> barberPerformance) {
        this.barberPerformance = barberPerformance;
    }

    public Long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(Long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public Long getWeekAppointments() {
        return weekAppointments;
    }

    public void setWeekAppointments(Long weekAppointments) {
        this.weekAppointments = weekAppointments;
    }

    public Long getMonthAppointments() {
        return monthAppointments;
    }

    public void setMonthAppointments(Long monthAppointments) {
        this.monthAppointments = monthAppointments;
    }
}

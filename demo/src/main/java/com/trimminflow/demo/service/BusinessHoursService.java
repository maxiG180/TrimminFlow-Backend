package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.BusinessHoursResponse;
import com.trimminflow.demo.dto.SetBusinessHoursRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.BusinessHours;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.BusinessHoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusinessHoursService {
    private final BusinessHoursRepository businessHoursRepository;
    private final BarbershopRepository barbershopRepository;

    // Valid days of the week
    private static final List<String> VALID_DAYS = Arrays.asList(
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );

    public BusinessHoursService(BusinessHoursRepository businessHoursRepository, BarbershopRepository barbershopRepository) {
        this.businessHoursRepository = businessHoursRepository;
        this.barbershopRepository = barbershopRepository;
    }

    @Transactional
    public BusinessHoursResponse setBusinessHours(UUID barbershopId, SetBusinessHoursRequest request) {
        // Validate barbershop exists
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbershop not found"));

        // Validate day of week
        if (request.getDayOfWeek() == null || request.getDayOfWeek().trim().isEmpty()) {
            throw new RuntimeException("Day of week is required");
        }

        String dayOfWeek = request.getDayOfWeek().trim().toUpperCase();
        if (!VALID_DAYS.contains(dayOfWeek)) {
            throw new RuntimeException("Invalid day of week. Must be one of: " + String.join(", ", VALID_DAYS));
        }

        // Validate isOpen is provided
        if (request.getIsOpen() == null) {
            throw new RuntimeException("isOpen field is required");
        }

        // If shop is open, validate time range
        if (request.getIsOpen()) {
            if (request.getOpenTime() == null || request.getCloseTime() == null) {
                throw new RuntimeException("Open and close times are required when shop is marked as open");
            }

            // Validate that open time is before close time
            LocalTime openTime = request.getOpenTime();
            LocalTime closeTime = request.getCloseTime();

            if (openTime.equals(closeTime)) {
                throw new RuntimeException("Open time and close time cannot be the same");
            }

            if (openTime.isAfter(closeTime)) {
                throw new RuntimeException("Open time must be before close time");
            }

            // Validate reasonable business hours (optional, but good practice)
            // Minimum 1 hour operation
            if (openTime.plusHours(1).isAfter(closeTime)) {
                throw new RuntimeException("Business must be open for at least 1 hour");
            }

            // Maximum 24 hours operation (sanity check)
            if (openTime.equals(LocalTime.MIDNIGHT) && closeTime.equals(LocalTime.MIDNIGHT)) {
                throw new RuntimeException("Invalid 24-hour operation time range");
            }
        }

        // Find existing or create new business hours
        BusinessHours businessHours = businessHoursRepository
                .findByBarbershopIdAndDayOfWeek(barbershopId, dayOfWeek)
                .orElse(new BusinessHours(barbershop, dayOfWeek, request.getIsOpen(), request.getOpenTime(), request.getCloseTime()));

        // Update the business hours
        businessHours.setIsOpen(request.getIsOpen());

        // Only set times if shop is open
        if (request.getIsOpen()) {
            businessHours.setOpenTime(request.getOpenTime());
            businessHours.setCloseTime(request.getCloseTime());
        } else {
            // Clear times if shop is closed
            businessHours.setOpenTime(null);
            businessHours.setCloseTime(null);
        }

        BusinessHours saved = businessHoursRepository.save(businessHours);
        return new BusinessHoursResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BusinessHoursResponse> getAllBusinessHours(UUID barbershopId) {
        return businessHoursRepository.findByBarbershopId(barbershopId).stream()
                .map(BusinessHoursResponse::new)
                .collect(Collectors.toList());
    }
}

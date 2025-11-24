package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.BusinessHoursResponse;
import com.trimminflow.demo.dto.SetBusinessHoursRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.BusinessHours;
import com.trimminflow.demo.entity.DayOfWeek;
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

    public BusinessHoursService(BusinessHoursRepository businessHoursRepository,
            BarbershopRepository barbershopRepository) {
        this.businessHoursRepository = businessHoursRepository;
        this.barbershopRepository = barbershopRepository;
    }

    @Transactional
    public BusinessHoursResponse setBusinessHours(UUID barbershopId, SetBusinessHoursRequest request) {
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbershop not found"));

        DayOfWeek dayOfWeek = request.getDayOfWeek();
        if (dayOfWeek == null) {
            throw new RuntimeException("Day of week is required");
        }

        if (request.getIsOpen() == null) {
            throw new RuntimeException("isOpen field is required");
        }

        if (request.getIsOpen()) {
            if (request.getOpenTime() == null || request.getCloseTime() == null) {
                throw new RuntimeException("Open and close times are required when shop is marked as open");
            }

            LocalTime openTime = request.getOpenTime();
            LocalTime closeTime = request.getCloseTime();

            if (openTime.equals(closeTime)) {
                throw new RuntimeException("Open time and close time cannot be the same");
            }

            if (openTime.isAfter(closeTime)) {
                throw new RuntimeException("Open time must be before close time");
            }

            // minimum 1 hour operation
            if (openTime.plusHours(1).isAfter(closeTime)) {
                throw new RuntimeException("Business must be open for at least 1 hour");
            }

            // maximum 24 hours check
            if (openTime.equals(LocalTime.MIDNIGHT) && closeTime.equals(LocalTime.MIDNIGHT)) {
                throw new RuntimeException("Invalid 24-hour operation time range");
            }
        }

        BusinessHours businessHours = businessHoursRepository
                .findByBarbershopIdAndDayOfWeek(barbershopId, dayOfWeek)
                .orElse(new BusinessHours(barbershop, dayOfWeek, request.getIsOpen(), request.getOpenTime(),
                        request.getCloseTime()));

        businessHours.setDayOfWeek(dayOfWeek);
        businessHours.setBarbershop(barbershop);

        businessHours.setIsOpen(request.getIsOpen());

        if (request.getIsOpen()) {
            businessHours.setOpenTime(request.getOpenTime());
            businessHours.setCloseTime(request.getCloseTime());
        } else {
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

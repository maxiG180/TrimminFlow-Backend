package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.BusinessHoursResponse;
import com.trimminflow.demo.dto.SetBusinessHoursRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.BusinessHours;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.BusinessHoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusinessHoursService {
    private final BusinessHoursRepository businessHoursRepository;
    private final BarbershopRepository barbershopRepository;

    public BusinessHoursService(BusinessHoursRepository businessHoursRepository, BarbershopRepository barbershopRepository) {
        this.businessHoursRepository = businessHoursRepository;
        this.barbershopRepository = barbershopRepository;
    }

    @Transactional
    public BusinessHoursResponse setBusinessHours(UUID barbershopId, SetBusinessHoursRequest request) {
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbershop not found"));

        BusinessHours businessHours = businessHoursRepository
                .findByBarbershopIdAndDayOfWeek(barbershopId, request.getDayOfWeek())
                .orElse(new BusinessHours(barbershop, request.getDayOfWeek(), request.getIsOpen(), request.getOpenTime(), request.getCloseTime()));

        businessHours.setIsOpen(request.getIsOpen());
        businessHours.setOpenTime(request.getOpenTime());
        businessHours.setCloseTime(request.getCloseTime());

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

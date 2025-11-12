package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.BusinessHoursResponse;
import com.trimminflow.demo.dto.ErrorResponse;
import com.trimminflow.demo.dto.SetBusinessHoursRequest;
import com.trimminflow.demo.service.BusinessHoursService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-hours")
@Tag(name = "Business Hours", description = "Business hours management APIs")
public class BusinessHoursController {
    private final BusinessHoursService businessHoursService;

    public BusinessHoursController(BusinessHoursService businessHoursService) {
        this.businessHoursService = businessHoursService;
    }

    @PostMapping
    @Operation(summary = "Set business hours", description = "Set or update business hours for a specific day")
    public ResponseEntity<?> setBusinessHours(
            @Valid @RequestBody SetBusinessHoursRequest request,
            @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            BusinessHoursResponse response = businessHoursService.setBusinessHours(barbershopId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Failed to set business hours", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all business hours", description = "Get business hours for all days of the week")
    public ResponseEntity<List<BusinessHoursResponse>> getAllBusinessHours(
            @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        List<BusinessHoursResponse> businessHours = businessHoursService.getAllBusinessHours(barbershopId);
        return ResponseEntity.ok(businessHours);
    }
}

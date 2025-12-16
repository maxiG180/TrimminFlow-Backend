package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.AnalyticsResponse;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Analytics and statistics APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    public AnalyticsController(AnalyticsService analyticsService, UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Unauthorized");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    @Operation(summary = "Get analytics", description = "Get barbershop analytics and statistics")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@RequestParam(required = false) String period) {
        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        AnalyticsResponse analytics = analyticsService.getAnalytics(barbershopId, period);
        return ResponseEntity.ok(analytics);
    }
}

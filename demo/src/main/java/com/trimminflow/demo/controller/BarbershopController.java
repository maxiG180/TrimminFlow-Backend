package com.trimminflow.demo.controller;

import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.service.BarbershopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbershops")
@Tag(name = "Barbershop", description = "Barbershop management APIs")
public class BarbershopController {

    private final BarbershopService barbershopService;
    private final UserRepository userRepository;

    public BarbershopController(BarbershopService barbershopService, UserRepository userRepository) {
        this.barbershopService = barbershopService;
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
    @Operation(summary = "Get all barbershops", description = "Retrieve a list of all barbershops")
    public ResponseEntity<List<Barbershop>> getAllBarbershops() {
        return ResponseEntity.ok(barbershopService.getAllBarbershops());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get barbershop by ID", description = "Retrieve a barbershop by its ID")
    public ResponseEntity<Barbershop> getBarbershopById(@PathVariable UUID id) {
        return barbershopService.getBarbershopById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new barbershop", description = "Create a new barbershop (Public for registration)")
    public ResponseEntity<Barbershop> createBarbershop(@RequestBody Barbershop barbershop) {
        // This is likely used during registration, so it might be public or protected.
        // If public, we can't enforce auth.
        // Assuming public for now as part of registration flow.
        Barbershop created = barbershopService.createBarbershop(barbershop);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update barbershop", description = "Update an existing barbershop by ID (Protected)")
    public ResponseEntity<Barbershop> updateBarbershop(@PathVariable UUID id, @RequestBody Barbershop barbershop) {
        try {
            User user = getAuthenticatedUser();
            // Ensure user owns this barbershop
            if (!user.getBarbershop().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Barbershop updated = barbershopService.updateBarbershop(id, barbershop);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete barbershop", description = "Delete a barbershop by ID (Protected)")
    public ResponseEntity<Void> deleteBarbershop(@PathVariable UUID id) {
        try {
            User user = getAuthenticatedUser();
            // Ensure user owns this barbershop
            if (!user.getBarbershop().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            barbershopService.deleteBarbershop(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

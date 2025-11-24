package com.trimminflow.demo.controller;

import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.service.BarbershopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbershops")
@Tag(name = "Barbershop", description = "Barbershop management APIs")
public class BarbershopController {

    private final BarbershopService barbershopService;

    public BarbershopController(BarbershopService barbershopService) {
        this.barbershopService = barbershopService;
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
    @Operation(summary = "Create a new barbershop", description = "Create a new barbershop")
    public ResponseEntity<Barbershop> createBarbershop(@RequestBody Barbershop barbershop) {
        Barbershop created = barbershopService.createBarbershop(barbershop);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update barbershop", description = "Update an existing barbershop by ID")
    public ResponseEntity<Barbershop> updateBarbershop(@PathVariable UUID id, @RequestBody Barbershop barbershop) {
        try {
            Barbershop updated = barbershopService.updateBarbershop(id, barbershop);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete barbershop", description = "Delete a barbershop by ID")
    public ResponseEntity<Void> deleteBarbershop(@PathVariable UUID id) {
        barbershopService.deleteBarbershop(id);
        return ResponseEntity.noContent().build();
    }
}

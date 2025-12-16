package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.AppointmentResponse;
import com.trimminflow.demo.dto.CustomerResponse;
import com.trimminflow.demo.entity.Appointment;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.Customer;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.AppointmentRepository;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.CustomerRepository;
import com.trimminflow.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final BarbershopRepository barbershopRepository;

    public CustomerController(CustomerRepository customerRepository, AppointmentRepository appointmentRepository,
            UserRepository userRepository, BarbershopRepository barbershopRepository) {
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.barbershopRepository = barbershopRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Barbershop getMyBarbershop() {
        User user = getAuthenticatedUser();
        if (user.getBarbershop() == null) {
            throw new RuntimeException("Barbershop not found for this user");
        }
        return user.getBarbershop();
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getCustomers(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Barbershop barbershop = getMyBarbershop();

        Page<Customer> customers;
        if (search != null && !search.isBlank()) {
            customers = customerRepository.searchCustomers(barbershop.getId(), search, pageable);
        } else {
            customers = customerRepository.findByBarbershopId(barbershop.getId(), pageable);
        }

        return ResponseEntity.ok(customers.map(CustomerResponse::fromEntity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID id) {
        Barbershop barbershop = getMyBarbershop();
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getBarbershop().getId().equals(barbershop.getId())) {
            throw new RuntimeException("Customer does not belong to your barbershop");
        }

        return ResponseEntity.ok(CustomerResponse.fromEntity(customer));
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<Page<AppointmentResponse>> getCustomerAppointments(
            @PathVariable UUID id,
            Pageable pageable) {
        Barbershop barbershop = getMyBarbershop();
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getBarbershop().getId().equals(barbershop.getId())) {
            throw new RuntimeException("Customer does not belong to your barbershop");
        }

        Page<Appointment> appointments = appointmentRepository.findByCustomerIdOrderByAppointmentDateTimeDesc(id,
                pageable);
        return ResponseEntity.ok(appointments.map(AppointmentResponse::fromEntity));
    }
}

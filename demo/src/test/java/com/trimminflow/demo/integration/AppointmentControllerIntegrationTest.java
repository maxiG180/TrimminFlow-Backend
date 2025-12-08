package com.trimminflow.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trimminflow.demo.dto.CreateAppointmentRequest;
import com.trimminflow.demo.entity.*;
import com.trimminflow.demo.repository.*;
import com.trimminflow.demo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BarbershopRepository barbershopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwtToken;
    private Barbershop testBarbershop;
    private Barber testBarber;
    private com.trimminflow.demo.entity.Service testService;

    @BeforeEach
    void setUp() {
        // Create test barbershop
        testBarbershop = new Barbershop();
        testBarbershop.setName("Test Barbershop");
        testBarbershop.setAddress("123 Test St");
        testBarbershop.setPhone("1234567890");
        testBarbershop = barbershopRepository.save(testBarbershop);

        // Create test user
        User testUser = new User();
        testUser.setEmail("admin@test.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(UserRole.ADMIN);
        testUser.setBarbershop(testBarbershop);
        testUser = userRepository.save(testUser);

        // Create test barber
        testBarber = new Barber();
        testBarber.setFirstName("John");
        testBarber.setLastName("Doe");
        testBarber.setEmail("john@test.com");
        testBarber.setBarbershop(testBarbershop);
        testBarber = barberRepository.save(testBarber);

        // Create test service
        testService = new com.trimminflow.demo.entity.Service();
        testService.setName("Haircut");
        testService.setPrice(BigDecimal.valueOf(25.00));
        testService.setDurationMinutes(30);
        testService.setBarbershop(testBarbershop);
        testService = serviceRepository.save(testService);

        // Generate JWT token
        jwtToken = jwtUtil.generateToken(testUser.getEmail(), testUser.getId(), testUser.getRole().name());
    }

    @Test
    void createAppointment_Success() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setBarberId(testBarber.getId());
        request.setServiceId(testService.getId());
        request.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        request.setCustomerName("Jane Customer");
        request.setCustomerEmail("jane@test.com");
        request.setCustomerPhone("1234567890");

        mockMvc.perform(post("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Jane Customer"))
                .andExpect(jsonPath("$.customerEmail").value("jane@test.com"));
    }

    @Test
    void getAllAppointments_Success() throws Exception {
        mockMvc.perform(get("/api/v1/appointments")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createAppointment_Unauthorized_Returns401() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setBarberId(testBarber.getId());
        request.setServiceId(testService.getId());
        request.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        request.setCustomerName("Jane Customer");
        request.setCustomerEmail("jane@test.com");

        mockMvc.perform(post("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

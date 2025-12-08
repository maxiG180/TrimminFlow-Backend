package com.trimminflow.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trimminflow.demo.dto.CreateServiceRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.entity.UserRole;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ServiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BarbershopRepository barbershopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwtToken;
    private Barbershop testBarbershop;

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

        // Generate JWT token
        jwtToken = jwtUtil.generateToken(testUser.getEmail(), testUser.getId(), testUser.getRole().name());
    }

    @Test
    void createService_Success() throws Exception {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Haircut");
        request.setDescription("Classic haircut");
        request.setPrice(BigDecimal.valueOf(25.00));
        request.setDurationMinutes(30);

        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Haircut"))
                .andExpect(jsonPath("$.durationMinutes").value(30));
    }

    @Test
    void getAllServices_Success() throws Exception {
        mockMvc.perform(get("/api/v1/services")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createService_Unauthorized_Returns401() throws Exception {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Haircut");
        request.setPrice(BigDecimal.valueOf(25.00));
        request.setDurationMinutes(30);

        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

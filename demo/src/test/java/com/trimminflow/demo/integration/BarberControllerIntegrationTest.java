package com.trimminflow.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trimminflow.demo.dto.CreateBarberRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BarberControllerIntegrationTest {

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
    void createBarber_Success() throws Exception {
        CreateBarberRequest request = new CreateBarberRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@test.com");
        request.setPhone("9876543210");

        mockMvc.perform(multipart("/api/v1/barbers")
                .param("firstName", request.getFirstName())
                .param("lastName", request.getLastName())
                .param("email", request.getEmail())
                .param("phone", request.getPhone())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void getAllBarbers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/barbers")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createBarber_Unauthorized_Returns401() throws Exception {
        CreateBarberRequest request = new CreateBarberRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@test.com");

        mockMvc.perform(multipart("/api/v1/barbers")
                .param("firstName", request.getFirstName())
                .param("lastName", request.getLastName())
                .param("email", request.getEmail()))
                .andExpect(status().isUnauthorized());
    }
}

package com.trimminflow.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trimminflow.demo.config.SecurityConfig;
import com.trimminflow.demo.dto.AppointmentResponse;
import com.trimminflow.demo.dto.CreateAppointmentRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.security.JwtAuthenticationFilter;
import com.trimminflow.demo.security.JwtUtil;
import com.trimminflow.demo.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Barbershop barbershop;
    private UUID barbershopId;

    @BeforeEach
    void setUp() {
        barbershopId = UUID.randomUUID();
        barbershop = new Barbershop();
        barbershop.setId(barbershopId);

        user = new User();
        user.setEmail("test@example.com");
        user.setBarbershop(barbershop);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("test@example.com",
                null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createAppointment_Success() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        AppointmentResponse response = new AppointmentResponse();
        response.setCustomerName("Jane Doe");

        when(appointmentService.createAppointment(eq(barbershopId), any(CreateAppointmentRequest.class)))
                .thenReturn(response);

        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setBarberId(UUID.randomUUID());
        request.setServiceId(UUID.randomUUID());
        request.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        request.setCustomerName("Jane Doe");
        request.setCustomerEmail("jane@example.com");

        mockMvc.perform(post("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Jane Doe"));
    }
}

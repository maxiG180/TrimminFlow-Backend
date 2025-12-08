package com.trimminflow.demo.controller;

import com.trimminflow.demo.config.SecurityConfig;
import com.trimminflow.demo.dto.CreateServiceRequest;
import com.trimminflow.demo.dto.ServiceResponse;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.security.JwtAuthenticationFilter;
import com.trimminflow.demo.security.JwtUtil;
import com.trimminflow.demo.service.ServiceManagementService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
public class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.trimminflow.demo.security.RateLimitFilter rateLimitFilter;

    @MockBean
    private ServiceManagementService serviceManagementService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

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

        // Configure RateLimitFilter to pass requests through
        try {
            doAnswer(invocation -> {
                jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
                return null;
            }).when(rateLimitFilter).doFilter(any(), any(), any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createService_Success() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        ServiceResponse response = new ServiceResponse();
        response.setName("Haircut");
        response.setPrice(BigDecimal.valueOf(25.0));

        when(serviceManagementService.createService(eq(barbershopId), any(CreateServiceRequest.class)))
                .thenReturn(response);

        String jsonRequest = "{\"name\":\"Haircut\", \"price\": 25.0, \"durationMinutes\": 30}";

        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Haircut"))
                .andExpect(jsonPath("$.price").value(25.0));
    }
}

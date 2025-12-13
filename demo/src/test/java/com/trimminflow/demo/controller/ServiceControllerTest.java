package com.trimminflow.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trimminflow.demo.dto.CreateServiceRequest;
import com.trimminflow.demo.dto.ServiceResponse;
import com.trimminflow.demo.dto.UpdateServiceRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.security.JwtAuthenticationFilter;
import com.trimminflow.demo.security.RateLimitFilter;
import com.trimminflow.demo.service.ServiceManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceController.class)
// Enable filters to allow SecurityContextHolder to work
public class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceManagementService serviceManagementService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private Barbershop mockBarbershop;
    private final String TEST_EMAIL = "owner@test.com";

    @BeforeEach
    void setUp() throws Exception {
        mockBarbershop = new Barbershop();
        mockBarbershop.setId(UUID.randomUUID());

        mockUser = new User();
        mockUser.setEmail(TEST_EMAIL);
        mockUser.setBarbershop(mockBarbershop);

        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(mockUser));

        // Stub filters
        doAnswer(invocation -> {
            ((jakarta.servlet.FilterChain) invocation.getArgument(2))
                    .doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            ((jakarta.servlet.FilterChain) invocation.getArgument(2))
                    .doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void createService_ShouldReturnCreated() throws Exception {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Haircut");
        request.setPrice(BigDecimal.valueOf(20.0));
        request.setDurationMinutes(30);

        ServiceResponse response = new ServiceResponse();
        response.setId(UUID.randomUUID());
        response.setName("Haircut");

        given(serviceManagementService.createService(eq(mockBarbershop.getId()), any(CreateServiceRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Haircut"));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void getAllServices_ShouldReturnList() throws Exception {
        ServiceResponse service = new ServiceResponse();
        service.setName("Haircut");

        given(serviceManagementService.getAllServices(mockBarbershop.getId()))
                .willReturn(Collections.singletonList(service));

        mockMvc.perform(get("/api/v1/services/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Haircut"));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void getService_ShouldReturnService() throws Exception {
        UUID serviceId = UUID.randomUUID();
        ServiceResponse service = new ServiceResponse();
        service.setId(serviceId);

        given(serviceManagementService.getService(serviceId, mockBarbershop.getId())).willReturn(service);

        mockMvc.perform(get("/api/v1/services/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId.toString()));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void updateService_ShouldReturnUpdated() throws Exception {
        UUID serviceId = UUID.randomUUID();
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("Updated Haircut");

        ServiceResponse response = new ServiceResponse();
        response.setId(serviceId);
        response.setName("Updated Haircut");

        given(serviceManagementService.updateService(eq(serviceId), eq(mockBarbershop.getId()),
                any(UpdateServiceRequest.class)))
                .willReturn(response);

        mockMvc.perform(put("/api/v1/services/{id}", serviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Haircut"));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void deleteService_ShouldReturnNoContent() throws Exception {
        UUID serviceId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/services/{id}", serviceId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(serviceManagementService).deleteService(serviceId, mockBarbershop.getId());
    }
}

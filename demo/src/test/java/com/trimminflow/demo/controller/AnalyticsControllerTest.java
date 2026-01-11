package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.AnalyticsResponse;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "test@test.com", authorities = "ROLE_OWNER")
    void getAnalytics_ReturnsSuccessfully() throws Exception {
        // Given
        UUID barbershopId = UUID.randomUUID();
        Barbershop barbershop = new Barbershop();
        barbershop.setId(barbershopId);

        User user = new User();
        user.setEmail("test@test.com");
        user.setBarbershop(barbershop);

        AnalyticsResponse response = createAnalyticsResponse();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(analyticsService.getAnalytics(any(UUID.class), isNull())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/analytics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAppointments").value(10))
                .andExpect(jsonPath("$.totalRevenue").value(200.00));
    }

    @Test
    void getAnalytics_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/analytics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private AnalyticsResponse createAnalyticsResponse() {
        AnalyticsResponse response = new AnalyticsResponse();
        response.setTotalAppointments(10L);
        response.setCompletedAppointments(8L);
        response.setCancelledAppointments(1L);
        response.setNoShowAppointments(1L);
        response.setTotalRevenue(BigDecimal.valueOf(200.00));
        response.setAverageRevenue(BigDecimal.valueOf(20.00));
        response.setTodayAppointments(2L);
        response.setWeekAppointments(5L);
        response.setMonthAppointments(10L);
        response.setPopularServices(new ArrayList<>());
        response.setBarberPerformance(new ArrayList<>());
        return response;
    }
}

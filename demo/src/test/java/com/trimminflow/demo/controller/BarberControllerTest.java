package com.trimminflow.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trimminflow.demo.config.SecurityConfig;
import com.trimminflow.demo.dto.BarberResponse;
import com.trimminflow.demo.dto.CreateBarberRequest;
import com.trimminflow.demo.dto.PageResponse;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.security.JwtAuthenticationFilter;
import com.trimminflow.demo.security.JwtUtil;
import com.trimminflow.demo.service.BarberManagementService;
import com.trimminflow.demo.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BarberController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
public class BarberControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private BarberManagementService barberManagementService;

        @MockBean
        private CloudinaryService cloudinaryService;

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

                // Manually set up the SecurityContext with a String principal to match
                // Controller expectation
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                "test@example.com",
                                null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        void createBarber_Success() throws Exception {
                when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

                BarberResponse response = new BarberResponse();
                response.setFirstName("John");
                response.setLastName("Doe");

                when(barberManagementService.createBarber(eq(barbershopId), any(CreateBarberRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(multipart("/api/v1/barbers")
                                .param("firstName", "John")
                                .param("lastName", "Doe")
                                .param("email", "john@example.com"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.firstName").value("John"))
                                .andExpect(jsonPath("$.lastName").value("Doe"));
        }

        @Test
        void getAllBarbers_Success() throws Exception {
                when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

                BarberResponse response = new BarberResponse();
                response.setFirstName("John");

                PageResponse<BarberResponse> pageResponse = new PageResponse<BarberResponse>(
                                Collections.singletonList(response), 0, 10, 1, 1, true, true);

                when(barberManagementService.getAllBarbersPaginated(barbershopId, 0, 10))
                                .thenReturn(pageResponse);

                mockMvc.perform(get("/api/v1/barbers")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].firstName").value("John"));
        }
}

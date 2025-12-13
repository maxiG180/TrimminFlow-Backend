package com.trimminflow.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trimminflow.demo.dto.BarberResponse;
import com.trimminflow.demo.dto.CreateBarberRequest;
import com.trimminflow.demo.dto.PageResponse;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.security.JwtAuthenticationFilter;
import com.trimminflow.demo.security.RateLimitFilter;
import com.trimminflow.demo.service.BarberManagementService;
import com.trimminflow.demo.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(BarberController.class)
// Enable filters to allow SecurityContextHolder to work
public class BarberControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private BarberManagementService barberManagementService;

        @MockBean
        private CloudinaryService cloudinaryService;

        @MockBean
        private UserRepository userRepository;

        // Mock Security Filters required by SecurityConfig
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

                // Stub filters to allow request to proceed
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
        void createBarber_ShouldReturnCreated() throws Exception {
                MockMultipartFile image = new MockMultipartFile(
                                "image", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

                BarberResponse response = new BarberResponse();
                response.setId(UUID.randomUUID());
                response.setFirstName("John");

                given(cloudinaryService.uploadImage(any())).willReturn("http://url.com/img.jpg");
                given(barberManagementService.createBarber(eq(mockBarbershop.getId()), any(CreateBarberRequest.class)))
                                .willReturn(response);

                mockMvc.perform(multipart("/api/v1/barbers")
                                .file(image)
                                .param("firstName", "John")
                                .param("lastName", "Doe")
                                .param("email", "john@example.com")
                                .with(csrf()))
                                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.firstName").value("John"));
        }

        @Test
        @WithMockUser(username = TEST_EMAIL)
        void getAllBarbers_ShouldReturnPage() throws Exception {
                BarberResponse barber = new BarberResponse();
                barber.setFirstName("John");

                PageResponse<BarberResponse> pageResponse = new PageResponse<>();
                pageResponse.setContent(Collections.singletonList(barber));
                pageResponse.setTotalElements(1);

                given(barberManagementService.getAllBarbersPaginated(eq(mockBarbershop.getId()), any(Integer.class),
                                any(Integer.class)))
                                .willReturn(pageResponse);

                mockMvc.perform(get("/api/v1/barbers")
                                .param("page", "0")
                                .param("size", "10"))
                                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].firstName").value("John"));
        }

        @Test
        @WithMockUser(username = TEST_EMAIL)
        void getBarber_ShouldReturnBarber() throws Exception {
                UUID barberId = UUID.randomUUID();
                BarberResponse barber = new BarberResponse();
                barber.setId(barberId);

                given(barberManagementService.getBarber(barberId, mockBarbershop.getId())).willReturn(barber);

                mockMvc.perform(get("/api/v1/barbers/{id}", barberId))
                                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(barberId.toString()));
        }

        @Test
        @WithMockUser(username = TEST_EMAIL)
        void deleteBarber_ShouldReturnNoContent() throws Exception {
                UUID barberId = UUID.randomUUID();

                mockMvc.perform(delete("/api/v1/barbers/{id}", barberId)
                                .with(csrf()))
                                .andExpect(status().isNoContent());
        }
}

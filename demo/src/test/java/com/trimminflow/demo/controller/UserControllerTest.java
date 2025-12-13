package com.trimminflow.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.security.JwtAuthenticationFilter;
import com.trimminflow.demo.security.RateLimitFilter;
import com.trimminflow.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(UserController.class)
// Enable filters to allow SecurityContextHolder to work
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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
    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        mockBarbershop = new Barbershop();
        mockBarbershop.setId(UUID.randomUUID());

        mockUser = new User();
        mockUser.setId(USER_ID);
        mockUser.setEmail(TEST_EMAIL);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
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
    void getCurrentUser_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void getUserById_ShouldReturnUser_WhenAuthorized() throws Exception {
        given(userService.getUserById(USER_ID)).willReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void getUserById_ShouldReturnForbidden_WhenNotMe() throws Exception {
        UUID otherId = UUID.randomUUID();
        mockMvc.perform(get("/api/users/{id}", otherId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void updateUser_ShouldReturnUpdated_WhenAuthorized() throws Exception {
        User updateRequest = new User();
        updateRequest.setFirstName("Jane");

        User updatedUser = new User();
        updatedUser.setId(USER_ID);
        updatedUser.setFirstName("Jane");

        given(userService.updateUser(eq(USER_ID), any(User.class))).willReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }
}

package com.trimminflow.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trimminflow.demo.config.SecurityConfig;
import com.trimminflow.demo.dto.LoginRequest;
import com.trimminflow.demo.dto.LoginResponse;
import com.trimminflow.demo.dto.RegisterRequest;
import com.trimminflow.demo.dto.RegisterResponse;
import com.trimminflow.demo.security.JwtAuthenticationFilter;
import com.trimminflow.demo.security.JwtUtil;
import com.trimminflow.demo.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setBarbershopName("Test Shop");
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("1234567890");
        request.setAddress("123 Test St");

        RegisterResponse response = new RegisterResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "Barbershop registered successfully");

        when(authService.registerBarbershopOwner(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("Barbershop registered successfully"));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");

        LoginResponse response = new LoginResponse(
                "dummy-jwt-token",
                UUID.randomUUID(),
                "test@example.com",
                "John",
                "Doe",
                "ADMIN",
                UUID.randomUUID(),
                "Login successful");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", "dummy-jwt-token"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.accessToken").doesNotExist()); // Access token should be stripped from body
    }

    @Test
    public void testLoginFailure() throws Exception {
        LoginRequest request = new LoginRequest("wrong@example.com", "WrongPass");

        when(authService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}

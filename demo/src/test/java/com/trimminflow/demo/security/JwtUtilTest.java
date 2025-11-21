package com.trimminflow.demo.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testTokenGenerationAndValidation() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String role = "CUSTOMER";

        String token = jwtUtil.generateToken(email, userId, role);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token, email));
        assertEquals(email, jwtUtil.extractEmail(token));
        assertEquals(userId, jwtUtil.extractUserId(token));
        assertEquals(role, jwtUtil.extractRole(token));
    }
}
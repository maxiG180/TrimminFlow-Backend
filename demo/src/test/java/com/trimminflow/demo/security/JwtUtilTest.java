package com.trimminflow.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Manually inject values that are usually injected by @Value
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "SuperSecretKeyForTestingThatIsLongEnoughToSatisfyHMACRequirements");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
    }

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
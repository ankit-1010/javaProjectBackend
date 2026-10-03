package com.moneymate;

import com.moneymate.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtils.generateToken(1L, "user@example.com", "USER");
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("user@example.com", jwtUtils.getEmailFromToken(token));
        assertEquals("USER", jwtUtils.getRoleFromToken(token));
        assertEquals(1L, jwtUtils.getUserIdFromToken(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtils.validateJwtToken("invalid.token.here"));
    }
}

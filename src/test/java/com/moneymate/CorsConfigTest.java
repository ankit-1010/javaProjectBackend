package com.moneymate;

import com.moneymate.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CorsConfigTest {

    @Test
    void testCorsConfigurationDefaultLocalhost() {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:5173");

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/transactions");
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);

        assertNotNull(corsConfig);
        assertEquals(List.of("http://localhost:5173"), corsConfig.getAllowedOrigins());
        assertFalse(corsConfig.getAllowedOrigins().contains("*"), "Allowed origins must not contain wildcard *");

        assertTrue(corsConfig.getAllowedMethods().containsAll(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")));
        assertTrue(corsConfig.getAllowedHeaders().containsAll(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With")));
        assertTrue(corsConfig.getExposedHeaders().contains("Authorization"));
        assertTrue(Boolean.TRUE.equals(corsConfig.getAllowCredentials()));
    }

    @Test
    void testCorsConfigurationMultipleCommaSeparatedOrigins() {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:5173, https://moneymate.vercel.app, https://app.moneymate.com");

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);

        assertNotNull(corsConfig);
        assertEquals(
                List.of("http://localhost:5173", "https://moneymate.vercel.app", "https://app.moneymate.com"),
                corsConfig.getAllowedOrigins()
        );
        assertFalse(corsConfig.getAllowedOrigins().contains("*"));
    }

    @Test
    void testCorsConfigurationFallbackWhenEmptyString() {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", "   ");

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/categories");
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);

        assertNotNull(corsConfig);
        assertEquals(List.of("http://localhost:5173"), corsConfig.getAllowedOrigins());
    }
}

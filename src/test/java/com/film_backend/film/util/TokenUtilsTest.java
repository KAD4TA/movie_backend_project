package com.film_backend.film.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TokenUtilsTest {

    @Test
    public void testExtractTokenValidToken() {
        String token = "Bearer myValidToken123";
        String extractedToken = TokenUtils.extractToken(token);

        assertEquals("myValidToken123", extractedToken, "Token should be extracted correctly");
    }

    @Test
    public void testExtractTokenInvalidTokenFormat() {
        String token = "InvalidTokenFormat";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            TokenUtils.extractToken(token);
        }, "Expected extractToken to throw, but it didn't");

        assertEquals("Invalid token format: Bearer token required.", thrown.getMessage(), "Exception message should be correct");
    }

    @Test
    public void testExtractTokenNullToken() {
        String token = null;

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            TokenUtils.extractToken(token);
        }, "Expected extractToken to throw, but it didn't");

        assertEquals("Invalid token format: Bearer token required.", thrown.getMessage(), "Exception message should be correct");
    }
}

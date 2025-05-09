package com.film_backend.film.util;

public class TokenUtils {

    public static String extractToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid token format: Bearer token required.");
        }
        return token.substring(7); // Remove "Bearer " prefix
    }
}

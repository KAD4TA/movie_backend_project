package com.film_backend.film.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.film_backend.film.entity.User;
import com.film_backend.film.enums.Role;
import com.film_backend.film.repository.TokenBlacklistRepository;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    private User user;
    private final String secret = "thisisaverylongsecretkeyforjwt1234567890";
    private final Long expiration = 3600L;         // 1 hour
    private final Long refreshExpiration = 86400L; // 1 day

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", refreshExpiration);
    }

    @Test
    void shouldGenerateValidToken() {
        String token = jwtUtil.generateToken(user);
        assertNotNull(token);

        Claims claims = jwtUtil.extractClaims(token);
        assertEquals(user.getEmail(), claims.getSubject());
        assertEquals(user.getId(), claims.get("id", Long.class));
        assertEquals(user.getRole().name(), claims.get("role", String.class));
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        String token = jwtUtil.generateRefreshToken(user);
        assertNotNull(token);

        Claims claims = jwtUtil.extractClaims(token);
        assertEquals(user.getEmail(), claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void shouldExtractEmailFromToken() {
        String token = jwtUtil.generateToken(user);
        String email = jwtUtil.extractEmail(token);
        assertEquals(user.getEmail(), email);
    }

    @Test
    void shouldExtractIdFromToken() {
        String token = jwtUtil.generateToken(user);
        Long id = jwtUtil.extractId(token);
        assertEquals(user.getId(), id);
    }

    @Test
    void shouldExtractRoleFromToken() {
        String token = jwtUtil.generateToken(user);
        String role = jwtUtil.extractRole(token);
        assertEquals(user.getRole().name(), role);
    }

    @Test
    void shouldReturnUserIdFromToken() {
        String token = jwtUtil.generateToken(user);
        Long id = jwtUtil.getUserIdFromToken(token);
        assertEquals(user.getId(), id);
    }

    @Test
    void shouldReturnTrueForValidToken() {
        when(tokenBlacklistRepository.existsByToken(anyString())).thenReturn(false);
        String token = jwtUtil.generateToken(user);
        boolean result = jwtUtil.isTokenValid(token, user.getEmail());
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForTokenWithWrongEmail() {
        when(tokenBlacklistRepository.existsByToken(anyString())).thenReturn(false);
        String token = jwtUtil.generateToken(user);
        boolean result = jwtUtil.isTokenValid(token, "wrong@example.com");
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForBlacklistedToken() {
        when(tokenBlacklistRepository.existsByToken(anyString())).thenReturn(true);
        String token = jwtUtil.generateToken(user);
        boolean result = jwtUtil.isTokenValid(token, user.getEmail());
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); 
        String token = jwtUtil.generateToken(user);
        Thread.sleep(1500);
        boolean result = jwtUtil.isTokenValid(token, user.getEmail());
        assertFalse(result);
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.test";
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.extractClaims(invalidToken));
    }

    @Test
    void shouldThrowExceptionForInvalidSigningKey() {
        ReflectionTestUtils.setField(jwtUtil, "secret", "short");
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.generateToken(user));
    }
}

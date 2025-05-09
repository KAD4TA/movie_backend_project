package com.film_backend.film.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.film_backend.film.repository.TokenBlacklistRepository;
import com.film_backend.film.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testMissingAuthorizationHeader() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Authorization header is missing."));
    }

    @Test
    void testInvalidAuthorizationFormat() throws ServletException, IOException {
        request.addHeader("Authorization", "InvalidTokenFormat");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid Authorization header format."));
    }

    @Test
    void testTokenBlacklisted() throws ServletException, IOException {
        String token = "blacklistedToken";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenBlacklistRepository.existsByToken(token)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token is blacklisted."));
    }

    @Test
    void testExpiredToken() throws Exception {
        String token = "expiredToken";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenBlacklistRepository.existsByToken(token)).thenReturn(false);
        when(jwtUtil.extractEmail(token)).thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Expired"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token has expired."));
    }

    @Test
    void testMalformedToken() throws Exception {
        String token = "malformedToken";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenBlacklistRepository.existsByToken(token)).thenReturn(false);
        when(jwtUtil.extractEmail(token)).thenThrow(new io.jsonwebtoken.MalformedJwtException("Malformed"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Malformed JWT token."));
    }

    @Test
    void testInvalidToken() throws Exception {
        String token = "invalidToken";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenBlacklistRepository.existsByToken(token)).thenReturn(false);
        when(jwtUtil.extractEmail(token)).thenThrow(new RuntimeException("Invalid"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid token."));
    }

    @Test
    void testValidToken() throws Exception {
        String token = "validToken";
        String email = "user@example.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(tokenBlacklistRepository.existsByToken(token)).thenReturn(false);
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(true);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email, "", Collections.emptyList());

        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(response.getErrorMessage());
        assertEquals(200, response.getStatus()); // default for MockHttpServletResponse
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
        verify(filterChain).doFilter(request, response);
    }
}

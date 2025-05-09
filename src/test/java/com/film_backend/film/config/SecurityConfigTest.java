package com.film_backend.film.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.film_backend.film.security.JwtAuthenticationFilter;

@SpringBootTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void securityFilterChain_shouldBeCreated() {
        assertNotNull(securityFilterChain, "SecurityFilterChain bean should be created");
    }

    @Test
    void passwordEncoder_shouldEncodeAndMatchPassword() {
        // given
        String rawPassword = "testPassword";

        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should differ from raw password");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), "Encoded password should match raw password");
    }

    @Test
    void authenticationManager_shouldBeAvailable() {
        assertNotNull(authenticationManager, "AuthenticationManager bean should be available");
    }

    @Test
    void jwtAuthenticationFilter_shouldBeLoaded() {
        assertNotNull(jwtAuthenticationFilter, "JwtAuthenticationFilter should be injected properly");
    }
}

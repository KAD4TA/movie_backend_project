package com.film_backend.film.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.film_backend.film.dtos.request.LoginRequestDto;
import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.ApiErrorResponse;
import com.film_backend.film.dtos.response.AuthResponseDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.entity.RefreshToken;
import com.film_backend.film.entity.User;
import com.film_backend.film.repository.RefreshTokenRepository;
import com.film_backend.film.repository.TokenBlacklistRepository;
import com.film_backend.film.repository.UserRepository;
import com.film_backend.film.security.AuthService;
import com.film_backend.film.util.JwtUtil;
import com.film_backend.film.util.TokenUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final UserRepository userRepository;
    private static final ZoneId ZONE_ID = ZoneId.of("UTC");

    public AuthController(AuthService authService, RefreshTokenRepository refreshTokenRepository, 
                         JwtUtil jwtUtil, TokenBlacklistRepository tokenBlacklistRepository, 
                         UserRepository userRepository) {
        this.authService = authService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/register/user")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRequestDto dto) {
        return ResponseEntity.ok(authService.registerUser(dto));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<UserResponseDto> registerAdmin(@Valid @RequestBody UserRequestDto dto) {
        return ResponseEntity.ok(authService.registerAdmin(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
    
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Refresh token is required.");
            }
            String token = TokenUtils.extractToken(refreshToken);

            // Check if the refresh token is valid (includes blacklist check)
            String email = jwtUtil.extractEmail(token);
            if (!jwtUtil.isTokenValid(token, email)) {
                throw new IllegalArgumentException("Invalid or expired refresh token.");
            }

            // Find the user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));

            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(user);

            // Generate new refresh token and delete the old one
            String newRefreshToken = jwtUtil.generateRefreshToken(user);
            refreshTokenRepository.deleteByToken(token);

            // Save the new refresh token
            RefreshToken refreshTokenEntity = new RefreshToken();
            refreshTokenEntity.setToken(newRefreshToken);
            refreshTokenEntity.setUser(user);
            refreshTokenEntity.setCreatedAt(LocalDateTime.now(ZONE_ID));
            refreshTokenEntity.setExpiresAt(LocalDateTime.now(ZONE_ID).plusSeconds(jwtUtil.getRefreshExpiration()));
            refreshTokenRepository.save(refreshTokenEntity);

            return ResponseEntity.ok(
                    AuthResponseDto.builder()
                            .token(newAccessToken) // Changed from .accessToken to .token
                            .refreshToken(newRefreshToken)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiErrorResponse.builder()
                            .errorCode("INVALID_INPUT")
                            .error(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .timestamp(ZonedDateTime.now().toString())
                            .build()
            );
        }
    }
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ApiErrorResponse.builder()
                                .errorCode("INVALID_INPUT")
                                .error("Token is required.")
                                .status(HttpStatus.BAD_REQUEST.value())
                                .timestamp(ZonedDateTime.now().toString())
                                .build()
                );
            }
            String extractedToken = TokenUtils.extractToken(token);
            authService.logout(extractedToken);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiErrorResponse.builder()
                            .errorCode("INVALID_INPUT")
                            .error(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .timestamp(ZonedDateTime.now().toString())
                            .build()
            );
        }
    }
}
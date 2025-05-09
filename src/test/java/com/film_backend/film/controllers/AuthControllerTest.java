package com.film_backend.film.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    private UserRequestDto userRequestDto;
    private LoginRequestDto loginRequestDto;
    private UserResponseDto userResponseDto;
    private AuthResponseDto authResponseDto;
    private User user;
    private String validToken;
    private String extractedToken;
    

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-token";
        extractedToken = "valid-token";

        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("testUser");
        userRequestDto.setEmail("test@example.com");
        userRequestDto.setPassword("password123");

        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password123");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setUsername("testUser");
        userResponseDto.setEmail("test@example.com");

        authResponseDto = AuthResponseDto.builder()
                .token("access-token")
                .refreshToken("refresh-token")
                .build();

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testUser");
    }

    // Tests for registerUser
    @Test
    void registerUser_ValidData_ReturnsOkWithUserResponse() {
        // Arrange
        when(authService.registerUser(userRequestDto)).thenReturn(userResponseDto);

        // Act
        ResponseEntity<UserResponseDto> response = authController.registerUser(userRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponseDto, response.getBody());
        verify(authService).registerUser(userRequestDto);
        verifyNoMoreInteractions(authService);
    }

    // Tests for registerAdmin
    @Test
    void registerAdmin_ValidData_ReturnsOkWithUserResponse() {
        // Arrange
        when(authService.registerAdmin(userRequestDto)).thenReturn(userResponseDto);

        // Act
        ResponseEntity<UserResponseDto> response = authController.registerAdmin(userRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponseDto, response.getBody());
        verify(authService).registerAdmin(userRequestDto);
        verifyNoMoreInteractions(authService);
    }

    // Tests for login
    @Test
    void login_ValidCredentials_ReturnsOkWithAuthResponse() {
        // Arrange
        when(authService.login(loginRequestDto)).thenReturn(authResponseDto);

        // Act
        ResponseEntity<AuthResponseDto> response = authController.login(loginRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponseDto, response.getBody());
        verify(authService).login(loginRequestDto);
        verifyNoMoreInteractions(authService);
    }

    // Tests for refreshToken
    @Test
    void refreshToken_ValidToken_ReturnsOkWithAuthResponse() {
        // Arrange
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        when(jwtUtil.extractEmail(extractedToken)).thenReturn("test@example.com");
        when(jwtUtil.isTokenValid(extractedToken, "test@example.com")).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn(newAccessToken);
        when(jwtUtil.generateRefreshToken(user)).thenReturn(newRefreshToken);
        when(jwtUtil.getRefreshExpiration()).thenReturn(86400L); // 1 day in seconds

        // Act
        ResponseEntity<?> response = authController.refreshToken(validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponseDto responseBody = (AuthResponseDto) response.getBody();
        assertNotNull(responseBody);
        assertEquals(newAccessToken, responseBody.getToken());
        assertEquals(newRefreshToken, responseBody.getRefreshToken());
        verify(jwtUtil).extractEmail(extractedToken);
        verify(jwtUtil).isTokenValid(extractedToken, "test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtUtil).generateToken(user);
        verify(jwtUtil).generateRefreshToken(user);
        verify(refreshTokenRepository).deleteByToken(extractedToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verifyNoInteractions(authService, tokenBlacklistRepository);
    }

    @Test
    void refreshToken_NullToken_ReturnsBadRequestWithError() {
        // Act
        ResponseEntity<?> response = authController.refreshToken(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Refresh token is required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verifyNoInteractions(jwtUtil, userRepository, refreshTokenRepository, authService, tokenBlacklistRepository);
    }

    @Test
    void refreshToken_EmptyToken_ReturnsBadRequestWithError() {
        // Act
        ResponseEntity<?> response = authController.refreshToken("");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Refresh token is required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verifyNoInteractions(jwtUtil, userRepository, refreshTokenRepository, authService, tokenBlacklistRepository);
    }

    @Test
    void refreshToken_InvalidTokenFormat_ReturnsBadRequestWithError() {
        // Arrange
        String invalidToken = "InvalidToken";

        // Act
        ResponseEntity<?> response = authController.refreshToken(invalidToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Invalid token format: Bearer token required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verifyNoInteractions(jwtUtil, userRepository, refreshTokenRepository, authService, tokenBlacklistRepository);
    }

    @Test
    void refreshToken_InvalidOrExpiredToken_ReturnsBadRequestWithError() {
        // Arrange
        when(jwtUtil.extractEmail(extractedToken)).thenReturn("test@example.com");
        when(jwtUtil.isTokenValid(extractedToken, "test@example.com")).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.refreshToken(validToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Invalid or expired refresh token.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(jwtUtil).extractEmail(extractedToken);
        verify(jwtUtil).isTokenValid(extractedToken, "test@example.com");
        verifyNoInteractions(userRepository, refreshTokenRepository, authService, tokenBlacklistRepository);
    }

    @Test
    void refreshToken_UserNotFound_ReturnsBadRequestWithError() {
        // Arrange
        when(jwtUtil.extractEmail(extractedToken)).thenReturn("test@example.com");
        when(jwtUtil.isTokenValid(extractedToken, "test@example.com")).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authController.refreshToken(validToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("User not found.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(jwtUtil).extractEmail(extractedToken);
        verify(jwtUtil).isTokenValid(extractedToken, "test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        verifyNoInteractions(refreshTokenRepository, authService, tokenBlacklistRepository);
    }

    // Tests for logout
    @Test
    void logout_ValidToken_ReturnsOk() {
        // Arrange
        doNothing().when(authService).logout(extractedToken);

        // Act
        ResponseEntity<?> response = authController.logout(validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(authService).logout(extractedToken);
        verifyNoMoreInteractions(authService);
        verifyNoInteractions(jwtUtil, userRepository, refreshTokenRepository, tokenBlacklistRepository);
    }

    @Test
    void logout_NullToken_ReturnsBadRequestWithError() {
        // Act
        ResponseEntity<?> response = authController.logout(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Token is required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verifyNoInteractions(authService, jwtUtil, userRepository, refreshTokenRepository, tokenBlacklistRepository);
    }

    @Test
    void logout_EmptyToken_ReturnsBadRequestWithError() {
        // Act
        ResponseEntity<?> response = authController.logout("");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Token is required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verifyNoInteractions(authService, jwtUtil, userRepository, refreshTokenRepository, tokenBlacklistRepository);
    }

    @Test
    void logout_InvalidTokenFormat_ReturnsBadRequestWithError() {
        // Arrange
        String invalidToken = "InvalidToken";

        // Act
        ResponseEntity<?> response = authController.logout(invalidToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Invalid token format: Bearer token required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verifyNoInteractions(authService, jwtUtil, userRepository, refreshTokenRepository, tokenBlacklistRepository);
    }
}
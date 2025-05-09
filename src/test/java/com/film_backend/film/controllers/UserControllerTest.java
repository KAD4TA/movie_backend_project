package com.film_backend.film.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.ApiErrorResponse;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private String validToken;
    private String extractedToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-token";
        extractedToken = "valid-token";

        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("newUser");
        userRequestDto.setEmail("new@example.com");
        userRequestDto.setPassword("newPassword");
        userRequestDto.setProfileImage("https://example.com/image.jpg");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setUsername("newUser");
        userResponseDto.setEmail("new@example.com");
    }

    // Tests for updateProfile
    @Test
    void updateProfile_ValidTokenAndData_ReturnsOkWithUserResponse() {
        // Arrange
        when(userService.updateProfile(userRequestDto, extractedToken)).thenReturn(userResponseDto);

        // Act
        ResponseEntity<?> response = userController.updateProfile(userRequestDto, validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponseDto, response.getBody());
        verify(userService).updateProfile(userRequestDto, extractedToken);
    }

    @Test
    void updateProfile_InvalidTokenFormat_ReturnsBadRequestWithError() {
        // Arrange
        String invalidToken = "InvalidToken";

        // Act
        ResponseEntity<?> response = userController.updateProfile(userRequestDto, invalidToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Invalid token format: Bearer token required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService, never()).updateProfile(any(), anyString());
    }

    @Test
    void updateProfile_NullToken_ReturnsBadRequestWithError() {
        // Act
        ResponseEntity<?> response = userController.updateProfile(userRequestDto, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Invalid token format: Bearer token required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService, never()).updateProfile(any(), anyString());
    }

    @Test
    void updateProfile_ServiceThrowsException_ReturnsBadRequestWithError() {
        // Arrange
        when(userService.updateProfile(userRequestDto, extractedToken))
                .thenThrow(new IllegalArgumentException("User not found."));

        // Act
        ResponseEntity<?> response = userController.updateProfile(userRequestDto, validToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("User not found.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService).updateProfile(userRequestDto, extractedToken);
    }

    // Tests for uploadProfilePicture
    @Test
    void uploadProfilePicture_ValidTokenAndImage_ReturnsOkWithUserResponse() {
        // Arrange
        when(userService.updateProfile(userRequestDto, extractedToken)).thenReturn(userResponseDto);

        // Act
        ResponseEntity<?> response = userController.uploadProfilePicture(userRequestDto, validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponseDto, response.getBody());
        verify(userService).updateProfile(userRequestDto, extractedToken);
    }

    @Test
    void uploadProfilePicture_NullImage_ReturnsBadRequestWithError() {
        // Arrange
        userRequestDto.setProfileImage(null);

        // Act
        ResponseEntity<?> response = userController.uploadProfilePicture(userRequestDto, validToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Profile image is required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService, never()).updateProfile(any(), anyString());
    }

    @Test
    void uploadProfilePicture_EmptyImage_ReturnsBadRequestWithError() {
        // Arrange
        userRequestDto.setProfileImage("");

        // Act
        ResponseEntity<?> response = userController.uploadProfilePicture(userRequestDto, validToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Profile image is required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService, never()).updateProfile(any(), anyString());
    }

    @Test
    void uploadProfilePicture_Base64Image_ReturnsBadRequestWithError() {
        // Arrange
        userRequestDto.setProfileImage("data:image/jpeg;base64,/9j/4AAQSkZJRg==");

        // Act
        ResponseEntity<?> response = userController.uploadProfilePicture(userRequestDto, validToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Base64 images are not supported. Provide a URL or local file path.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService, never()).updateProfile(any(), anyString());
    }

    @Test
    void uploadProfilePicture_InvalidTokenFormat_ReturnsBadRequestWithError() {
        // Arrange
        String invalidToken = "InvalidToken";

        // Act
        ResponseEntity<?> response = userController.uploadProfilePicture(userRequestDto, invalidToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Invalid token format: Bearer token required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService, never()).updateProfile(any(), anyString());
    }

    @Test
    void uploadProfilePicture_ServiceThrowsException_ReturnsBadRequestWithError() {
        // Arrange
        when(userService.updateProfile(userRequestDto, extractedToken))
                .thenThrow(new IllegalArgumentException("Failed to save profile image."));

        // Act
        ResponseEntity<?> response = userController.uploadProfilePicture(userRequestDto, validToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Failed to save profile image.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService).updateProfile(userRequestDto, extractedToken);
    }

    // Tests for deleteProfile
    @Test
    void deleteProfile_ValidToken_ReturnsOk() {
        // Arrange
        doNothing().when(userService).deleteProfile(extractedToken);

        // Act
        ResponseEntity<?> response = userController.deleteProfile(validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).deleteProfile(extractedToken);
    }

    @Test
    void deleteProfile_InvalidTokenFormat_ReturnsBadRequestWithError() {
        // Arrange
        String invalidToken = "InvalidToken";

        // Act
        ResponseEntity<?> response = userController.deleteProfile(invalidToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Invalid token format: Bearer token required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService, never()).deleteProfile(anyString());
    }

    @Test
    void deleteProfile_NullToken_ReturnsBadRequestWithError() {
        // Act
        ResponseEntity<?> response = userController.deleteProfile(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("Invalid token format: Bearer token required.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService, never()).deleteProfile(anyString());
    }

    @Test
    void deleteProfile_ServiceThrowsException_ReturnsBadRequestWithError() {
        // Arrange
        doThrow(new IllegalArgumentException("User not found.")).when(userService).deleteProfile(extractedToken);

        // Act
        ResponseEntity<?> response = userController.deleteProfile(validToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_INPUT", errorResponse.getErrorCode());
        assertEquals("User not found.", errorResponse.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        verify(userService).deleteProfile(extractedToken);
    }
}
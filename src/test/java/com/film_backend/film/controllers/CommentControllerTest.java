package com.film_backend.film.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.film_backend.film.dtos.request.CommentRequestDto;
import com.film_backend.film.dtos.response.CommentResponseDto;
import com.film_backend.film.service.CommentService;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private CommentRequestDto commentRequestDto;
    private CommentResponseDto commentResponseDto;
    private String validToken;
    private String extractedToken;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-token";
        extractedToken = "valid-token";

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContent("Great movie!");
        commentRequestDto.setRate(5);
        commentRequestDto.setMovieId(1L);

        commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .username("testUser")
                .content("Great movie!")
                .createdAt(new Date())
                .updatedAt(new Date())
                .rate(5)
                .userId(1L)
                .movieId(1L)
                .build();

        // Set up validator for testing DTO validation
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createComment_ValidTokenAndData_ReturnsOkWithCommentResponse() {
        // Arrange
        when(commentService.createComment(commentRequestDto, extractedToken)).thenReturn(commentResponseDto);

        // Act
        ResponseEntity<?> response = commentController.createComment(validToken, commentRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(commentResponseDto, response.getBody());
        verify(commentService).createComment(commentRequestDto, extractedToken);
        verifyNoMoreInteractions(commentService);
    }

    @Test
    void createComment_NullToken_ReturnsForbiddenWithErrorMessage() {
        // Act
        ResponseEntity<?> response = commentController.createComment(null, commentRequestDto);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden: No token provided", response.getBody());
        verifyNoInteractions(commentService);
    }

    @Test
    void createComment_EmptyToken_ReturnsForbiddenWithErrorMessage() {
        // Act
        ResponseEntity<?> response = commentController.createComment("", commentRequestDto);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden: No token provided", response.getBody());
        verifyNoInteractions(commentService);
    }

    @Test
    void createComment_InvalidTokenFormat_ReturnsInternalServerErrorWithErrorMessage() {
        // Arrange
        String invalidToken = "InvalidToken";

        // Act
        ResponseEntity<?> response = commentController.createComment(invalidToken, commentRequestDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred: Invalid token format: Bearer token required.", response.getBody());
        verifyNoInteractions(commentService);
    }

    @Test
    void createComment_MalformedJwtToken_ReturnsBadRequestWithErrorMessage() {
        // Arrange
        when(commentService.createComment(commentRequestDto, extractedToken))
                .thenThrow(new MalformedJwtException("Invalid JWT token"));

        // Act
        ResponseEntity<?> response = commentController.createComment(validToken, commentRequestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid JWT token: Invalid JWT token", response.getBody());
        verify(commentService).createComment(commentRequestDto, extractedToken);
        verifyNoMoreInteractions(commentService);
    }

    @Test
    void createComment_UnexpectedException_ReturnsInternalServerErrorWithErrorMessage() {
        // Arrange
        when(commentService.createComment(commentRequestDto, extractedToken))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = commentController.createComment(validToken, commentRequestDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred: Database error", response.getBody());
        verify(commentService).createComment(commentRequestDto, extractedToken);
        verifyNoMoreInteractions(commentService);
    }

    @Test
    void createComment_BlankContent_ValidatesDtoButServiceCalled() {
        // Arrange
        commentRequestDto.setContent("");

        // Validate DTO
        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(commentRequestDto);
        assertFalse(violations.isEmpty());
        assertEquals("Content must not be blank", violations.iterator().next().getMessage());

        // Act
        ResponseEntity<?> response = commentController.createComment(validToken, commentRequestDto);

        // Assert
        // In unit tests, @Valid doesn't trigger validation, so service is called
        verify(commentService).createComment(commentRequestDto, extractedToken);
        verifyNoMoreInteractions(commentService);
    }

    @Test
    void createComment_NullRate_ValidatesDtoButServiceCalled() {
        // Arrange
        commentRequestDto.setRate(null);

        // Validate DTO
        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(commentRequestDto);
        assertFalse(violations.isEmpty());
        assertEquals("Rate is required", violations.iterator().next().getMessage());

        // Act
        ResponseEntity<?> response = commentController.createComment(validToken, commentRequestDto);

        // Assert
        verify(commentService).createComment(commentRequestDto, extractedToken);
        verifyNoMoreInteractions(commentService);
    }

    @Test
    void createComment_InvalidRateBelowMin_ValidatesDtoButServiceCalled() {
        // Arrange
        commentRequestDto.setRate(0);

        // Validate DTO
        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(commentRequestDto);
        assertFalse(violations.isEmpty());
        assertEquals("Rate must be at least 1", violations.iterator().next().getMessage());

        // Act
        ResponseEntity<?> response = commentController.createComment(validToken, commentRequestDto);

        // Assert
        verify(commentService).createComment(commentRequestDto, extractedToken);
        verifyNoMoreInteractions(commentService);
    }

    @Test
    void createComment_InvalidRateAboveMax_ValidatesDtoButServiceCalled() {
        // Arrange
        commentRequestDto.setRate(6);

        // Validate DTO
        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(commentRequestDto);
        assertFalse(violations.isEmpty());
        assertEquals("Rate cannot be more than 5", violations.iterator().next().getMessage());

        // Act
        ResponseEntity<?> response = commentController.createComment(validToken, commentRequestDto);

        // Assert
        verify(commentService).createComment(commentRequestDto, extractedToken);
        verifyNoMoreInteractions(commentService);
    }

    @Test
    void createComment_NullMovieId_ValidatesDtoButServiceCalled() {
        // Arrange
        commentRequestDto.setMovieId(null);

        // Validate DTO
        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(commentRequestDto);
        assertFalse(violations.isEmpty());
        assertEquals("Movie ID is required", violations.iterator().next().getMessage());

        // Act
        ResponseEntity<?> response = commentController.createComment(validToken, commentRequestDto);

        // Assert
        verify(commentService).createComment(commentRequestDto, extractedToken);
        verifyNoMoreInteractions(commentService);
    }
}
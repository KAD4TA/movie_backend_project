package com.film_backend.film.controllers;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.ApiErrorResponse;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.service.UserService;
import com.film_backend.film.util.TokenUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UserRequestDto dto,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String extractedToken = TokenUtils.extractToken(token);
            UserResponseDto response = userService.updateProfile(dto, extractedToken);
            return ResponseEntity.ok(response);
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

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/profile/picture")
    public ResponseEntity<?> uploadProfilePicture(
            @Valid @RequestBody UserRequestDto dto,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (dto.getProfileImage() == null || dto.getProfileImage().isEmpty()) {
                throw new IllegalArgumentException("Profile image is required.");
            }
            if (dto.getProfileImage().startsWith("data:image/")) {
                throw new IllegalArgumentException("Base64 images are not supported. Provide a URL or local file path.");
            }
            String extractedToken = TokenUtils.extractToken(token);
            UserResponseDto response = userService.updateProfile(dto, extractedToken);
            return ResponseEntity.ok(response);
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

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteProfile(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String extractedToken = TokenUtils.extractToken(token);
            userService.deleteProfile(extractedToken);
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
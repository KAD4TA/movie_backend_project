package com.film_backend.film.controllers;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.film_backend.film.dtos.request.MovieRequestDto;
import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.ApiErrorResponse;
import com.film_backend.film.dtos.response.MovieSimpleResponseDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.exception.UserNotFoundException;
import com.film_backend.film.service.AdminService;
import com.film_backend.film.service.MovieService;
import com.film_backend.film.util.TokenUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;
    private final MovieService movieService;

    public AdminController(AdminService adminService, MovieService movieService) {
        this.adminService = adminService;
        this.movieService = movieService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UserRequestDto dto,
            @RequestHeader("Authorization") String token) {
        try {
            String extractedToken = TokenUtils.extractToken(token);
           
            UserResponseDto response = adminService.updateAdmin(extractedToken, dto);
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
        } catch (IOException e) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiErrorResponse.builder()
                            .errorCode("IMAGE_PROCESSING_ERROR")
                            .error("Resim işlenemedi: " + e.getMessage())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .timestamp(ZonedDateTime.now().toString())
                            .build()
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/profile/picture")
    public ResponseEntity<?> uploadProfilePicture(
            @Valid @RequestBody UserRequestDto dto,
            @RequestHeader("Authorization") String token) {
        try {
            String extractedToken = TokenUtils.extractToken(token);
            
            UserResponseDto response = adminService.uploadProfilePicture(extractedToken, dto);
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
        } catch (IOException e) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiErrorResponse.builder()
                            .errorCode("IMAGE_PROCESSING_ERROR")
                            .error("Resim işlenemedi: " + e.getMessage())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .timestamp(ZonedDateTime.now().toString())
                            .build()
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/profile")
    public ResponseEntity<ApiErrorResponse> deleteProfile(@RequestHeader("Authorization") String token) {
        try {
            String extractedToken = TokenUtils.extractToken(token);
            
            adminService.deleteAdmin(extractedToken);
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
        } catch (IllegalStateException e) {
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiErrorResponse.builder()
                            .errorCode("LAST_ADMIN")
                            .error(e.getMessage())
                            .status(HttpStatus.FORBIDDEN.value())
                            .timestamp(ZonedDateTime.now().toString())
                            .build()
            );
        }
    }

   

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(@PageableDefault(page = 0, size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        try {
            UserResponseDto user = adminService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Movie-related endpoints (using better RESTful standards)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/movies")
    public ResponseEntity<MovieSimpleResponseDto> createMovie(@Valid @RequestBody MovieRequestDto dto) throws Exception {
        log.info("Received createMovie request: DTO={}", dto);
        if (dto == null) {
            log.error("MovieRequestDto is null");
            throw new IllegalArgumentException("Movie data is required");
        }
        return ResponseEntity.ok(movieService.createMovie(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/movies-update/{id}")
    public ResponseEntity<MovieSimpleResponseDto> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequestDto dto) throws Exception {
        log.info("Received updateMovie request: ID={}, DTO={}", id, dto);
        if (dto == null) {
            log.error("MovieRequestDto is null");
            throw new IllegalArgumentException("Movie data is required");
        }
        return ResponseEntity.ok(movieService.updateMovie(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        log.info("Deleting movie: ID={}", id);
        movieService.deleteMovie(id);
        return ResponseEntity.ok().build();
    }
}
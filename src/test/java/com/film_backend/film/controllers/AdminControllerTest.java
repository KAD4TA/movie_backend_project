package com.film_backend.film.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.film_backend.film.dtos.request.MovieRequestDto;
import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.MovieSimpleResponseDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.exception.UserNotFoundException;
import com.film_backend.film.service.AdminService;
import com.film_backend.film.service.MovieService;

public class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private AdminController adminController;

    private String bearerToken;
    private String extractedToken;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        bearerToken = "Bearer mock.token.value";
        extractedToken = "mock.token.value";
    }

    @Test
    void testUpdateProfile_success() throws IOException {
        UserRequestDto dto = new UserRequestDto();
        UserResponseDto expectedResponse = new UserResponseDto();

        when(adminService.updateAdmin(extractedToken, dto)).thenReturn(expectedResponse);

        ResponseEntity<?> response = adminController.updateProfile(dto, bearerToken);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(adminService).updateAdmin(extractedToken, dto);
    }

    @Test
    void testUploadProfilePicture_success() throws IOException {
        UserRequestDto dto = new UserRequestDto();
        UserResponseDto expectedResponse = new UserResponseDto();

        when(adminService.uploadProfilePicture(extractedToken, dto)).thenReturn(expectedResponse);

        ResponseEntity<?> response = adminController.uploadProfilePicture(dto, bearerToken);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(adminService).uploadProfilePicture(extractedToken, dto);
    }

    @Test
    void testDeleteProfile_success() {
        doNothing().when(adminService).deleteAdmin(extractedToken);

        ResponseEntity<?> response = adminController.deleteProfile(bearerToken);

        assertEquals(200, response.getStatusCode().value());
        verify(adminService).deleteAdmin(extractedToken);
    }

    @Test
    void testGetAllUsers_success() {
        Pageable pageable = PageRequest.of(0, 10);
        UserResponseDto user = new UserResponseDto();
        PageImpl<UserResponseDto> page = new PageImpl<>(Collections.singletonList(user));

        when(adminService.getAllUsers(pageable)).thenReturn(page);

        ResponseEntity<?> response = adminController.getAllUsers(pageable);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    @Test
    void testGetUserById_success() {
        UserResponseDto user = new UserResponseDto();

        when(adminService.getUserById(1L)).thenReturn(user);

        ResponseEntity<UserResponseDto> response = adminController.getUserById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUserById_notFound() {
        when(adminService.getUserById(1L)).thenThrow(new UserNotFoundException("Not found"));

        ResponseEntity<UserResponseDto> response = adminController.getUserById(1L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testCreateMovie_success() throws Exception {
        MovieRequestDto dto = new MovieRequestDto();
        MovieSimpleResponseDto expected = new MovieSimpleResponseDto();

        when(movieService.createMovie(dto)).thenReturn(expected);

        ResponseEntity<MovieSimpleResponseDto> response = adminController.createMovie(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expected, response.getBody());
    }

    @Test
    void testUpdateMovie_success() throws Exception {
        MovieRequestDto dto = new MovieRequestDto();
        MovieSimpleResponseDto expected = new MovieSimpleResponseDto();

        when(movieService.updateMovie(1L, dto)).thenReturn(expected);

        ResponseEntity<MovieSimpleResponseDto> response = adminController.updateMovie(1L, dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expected, response.getBody());
    }

    @Test
    void testDeleteMovie_success() {
        doNothing().when(movieService).deleteMovie(1L);

        ResponseEntity<Void> response = adminController.deleteMovie(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(movieService).deleteMovie(1L);
    }
}

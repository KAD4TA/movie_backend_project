package com.film_backend.film.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.film_backend.film.dtos.response.MovieDetailResponseDto;
import com.film_backend.film.dtos.response.MovieSimpleResponseDto;
import com.film_backend.film.service.MovieService;

class MovieControllerTest {

    @InjectMocks
    private MovieController movieController;

    @Mock
    private MovieService movieService;

    private MovieSimpleResponseDto movieSimpleResponseDto;
    private MovieDetailResponseDto movieDetailResponseDto;
    private List<MovieSimpleResponseDto> movieList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize DTOs
        movieSimpleResponseDto = new MovieSimpleResponseDto();
        // Add fields as needed, e.g., movieSimpleResponseDto.setTitle("Test Movie");

        movieDetailResponseDto = new MovieDetailResponseDto();
        // Add fields as needed, e.g., movieDetailResponseDto.setId(1L);

        movieList = Arrays.asList(movieSimpleResponseDto);
    }

    @Test
    void testListMovies_WithTitle_Success() {
        // Arrange
        String title = "Test Movie";
        when(movieService.listSimpleMovies(eq(title))).thenReturn(movieList);

        // Act
        ResponseEntity<List<MovieSimpleResponseDto>> response = movieController.listMovies(title);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(movieList, response.getBody());
        verify(movieService, times(1)).listSimpleMovies(title);
    }

    @Test
    void testListMovies_WithoutTitle_Success() {
        // Arrange
        when(movieService.listSimpleMovies(null)).thenReturn(movieList);

        // Act
        ResponseEntity<List<MovieSimpleResponseDto>> response = movieController.listMovies(null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(movieList, response.getBody());
        verify(movieService, times(1)).listSimpleMovies(null);
    }

    @Test
    void testListMovies_EmptyTitle_Success() {
        // Arrange
        String emptyTitle = "";
        when(movieService.listSimpleMovies(eq(emptyTitle))).thenReturn(movieList);

        // Act
        ResponseEntity<List<MovieSimpleResponseDto>> response = movieController.listMovies(emptyTitle);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(movieList, response.getBody());
        verify(movieService, times(1)).listSimpleMovies(emptyTitle);
    }

    @Test
    void testListMovies_ServiceThrowsException() {
        // Arrange
        String title = "Test Movie";
        when(movieService.listSimpleMovies(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid title"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> movieController.listMovies(title)
        );
        assertEquals("Invalid title", exception.getMessage());
        verify(movieService, times(1)).listSimpleMovies(title);
    }

    @Test
    void testGetMovieById_Success() {
        // Arrange
        Long movieId = 1L;
        when(movieService.getMovieById(eq(movieId))).thenReturn(movieDetailResponseDto);

        // Act
        ResponseEntity<MovieDetailResponseDto> response = movieController.getMovieById(movieId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(movieDetailResponseDto, response.getBody());
        verify(movieService, times(1)).getMovieById(movieId);
    }

    @Test
    void testGetMovieById_ServiceThrowsException() {
        // Arrange
        Long movieId = 999L;
        when(movieService.getMovieById(eq(movieId)))
                .thenThrow(new IllegalArgumentException("Movie not found"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> movieController.getMovieById(movieId)
        );
        assertEquals("Movie not found", exception.getMessage());
        verify(movieService, times(1)).getMovieById(movieId);
    }
}
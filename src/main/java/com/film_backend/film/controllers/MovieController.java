package com.film_backend.film.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.film_backend.film.dtos.response.MovieDetailResponseDto;
import com.film_backend.film.dtos.response.MovieSimpleResponseDto;
import com.film_backend.film.service.MovieService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<MovieSimpleResponseDto>> listMovies(@RequestParam(required = false) String title) {
        return ResponseEntity.ok(movieService.listSimpleMovies(title));
    }
    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailResponseDto> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }
}
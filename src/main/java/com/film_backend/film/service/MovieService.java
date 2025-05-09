package com.film_backend.film.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.film_backend.film.dtos.request.MovieRequestDto;
import com.film_backend.film.dtos.response.MovieDetailResponseDto;
import com.film_backend.film.dtos.response.MovieSimpleResponseDto;
import com.film_backend.film.entity.Movie;
import com.film_backend.film.mapper.MovieMapper;
import com.film_backend.film.repository.MovieRepository;

@Service
public class MovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public MovieService(MovieRepository movieRepository, MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    public MovieSimpleResponseDto createMovie(MovieRequestDto dto) throws Exception {
        log.info("Creating movie: DTO={}", dto);
        if (dto == null) {
            log.error("MovieRequestDto is null");
            throw new IllegalArgumentException("Movie data is required");
        }

        
        Movie movie = movieMapper.toEntity(dto);
        movie = movieRepository.save(movie);
        return movieMapper.toSimpleDTO(movie);
    }

    public MovieSimpleResponseDto updateMovie(Long id, MovieRequestDto dto) throws Exception {
        log.info("Updating movie: ID={}, DTO={}", id, dto);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Movie not found: ID={}", id);
                    return new RuntimeException("Movie not found");
                });

        if (dto == null) {
            log.error("MovieRequestDto is null");
            throw new IllegalArgumentException("Movie data is required");
        }

        movie.setTitle(dto.getTitle());
        movie.setGenres(dto.getGenres());
        movie.setVideoUrl(dto.getVideoUrl());
        movie.setPosterUrl(dto.getPosterUrl());
        movie.setDescription(dto.getDescription());
        movie.setDuration(dto.getDuration());
        movie.setReleaseYear(dto.getReleaseYear());
        movie = movieRepository.save(movie);
        return movieMapper.toSimpleDTO(movie);
    }

    public void deleteMovie(Long id) {
        log.info("Deleting movie: ID={}", id);
        movieRepository.deleteById(id);
    }

    public List<MovieSimpleResponseDto> listSimpleMovies(String title) {
        List<Movie> movies = (title == null || title.isEmpty())
                ? movieRepository.findAll()
                : movieRepository.findByTitleContainingIgnoreCase(title);

        return movies.stream()
                .map(movieMapper::toSimpleDTO)
                .collect(Collectors.toList());
    }
    
    public MovieDetailResponseDto getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        return movieMapper.toDetailDTO(movie);
    }
}
package com.film_backend.film.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.film_backend.film.dtos.request.MovieRequestDto;
import com.film_backend.film.dtos.response.MovieDetailResponseDto;
import com.film_backend.film.dtos.response.MovieSimpleResponseDto;
import com.film_backend.film.entity.Movie;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MovieMapper {
	
	private final CommentMapper commentMapper;

	public Movie toEntity(MovieRequestDto dto) {
	    if (dto.getGenres() == null || dto.getGenres().isEmpty()) {
	        throw new IllegalArgumentException("Genres cannot be null or empty");
	    }

	    return Movie.builder()
	            .title(dto.getTitle())
	            .genres(dto.getGenres())
	            .description(dto.getDescription())
	            .posterUrl(dto.getPosterUrl())
	            .videoUrl(dto.getVideoUrl())
	            .duration(dto.getDuration())
	            .releaseYear(dto.getReleaseYear())
	            .build();
	}
    
    
    public MovieSimpleResponseDto toSimpleDTO(Movie movie) {
        return MovieSimpleResponseDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .videoUrl(movie.getVideoUrl())
                
                .build();
    }

    public MovieDetailResponseDto toDetailDTO(Movie movie) {
        return MovieDetailResponseDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .genre(movie.getGenres())
                .posterUrl(movie.getPosterUrl())
                .description(movie.getDescription())
                .videoUrl(movie.getVideoUrl())
                .duration(movie.getDuration())
                .releaseYear(movie.getReleaseYear())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .averageRating(movie.getAverageRating())
                .comments(movie.getComments() == null ? List.of() :
                        movie.getComments().stream()
                             .map(commentMapper::toDTO)
                             .collect(Collectors.toList()))
                .build();
    }
}

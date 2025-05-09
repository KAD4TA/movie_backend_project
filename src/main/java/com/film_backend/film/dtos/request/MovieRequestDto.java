package com.film_backend.film.dtos.request;

import java.util.Set;

import com.film_backend.film.enums.Genre;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotEmpty(message = "Genres cannot be empty")
    private Set<Genre> genres;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Poster URL is required")
    private String posterUrl;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration cannot exceed 600 minutes")
    private Integer duration;

    @NotNull(message = "Release year is required")
    @Min(value = 1888, message = "Release year must be after 1888")
    @Max(value = 2100, message = "Release year is too far in the future")
    private Integer releaseYear;

    @NotBlank(message = "Video URL is required")
    private String videoUrl;
}
package com.film_backend.film.dtos.response;



import java.util.Date;
import java.util.List;
import java.util.Set;

import com.film_backend.film.enums.Genre;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDetailResponseDto{
    private Long id;
    private String title;
    private Set<Genre> genre;
    private String posterUrl;
    private String videoUrl;
    private String description;
    private Integer duration;
    private Integer releaseYear;
    private Date createdAt; 
    private Date updatedAt; 
    private Double averageRating;
    private List<CommentResponseDto> comments;
}
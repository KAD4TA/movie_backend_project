package com.film_backend.film.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieSimpleResponseDto {
    private Long id;
    private String title;
    private String posterUrl;
    private String videoUrl;
    
}
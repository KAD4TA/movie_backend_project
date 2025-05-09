package com.film_backend.film.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequestDto {

    @NotBlank(message = "Content must not be blank")
    private String content;

    @NotNull(message = "Rate is required")
    @Min(value = 1, message = "Rate must be at least 1")
    @Max(value = 5, message = "Rate cannot be more than 5")
    
    private Integer rate; 

    @NotNull(message = "Movie ID is required")
    private Long movieId;
}

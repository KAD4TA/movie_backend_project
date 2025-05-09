package com.film_backend.film.dtos.response;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private Long id;
    private String username;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private Integer rate;
    private Long userId;
    private Long movieId;
	
}
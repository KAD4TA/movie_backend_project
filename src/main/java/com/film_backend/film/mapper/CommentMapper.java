package com.film_backend.film.mapper;

import org.springframework.stereotype.Component;

import com.film_backend.film.dtos.request.CommentRequestDto;
import com.film_backend.film.dtos.response.CommentResponseDto;
import com.film_backend.film.entity.Comment;
import com.film_backend.film.entity.Movie;
import com.film_backend.film.entity.User;

@Component
public class CommentMapper {

    public Comment toEntity(CommentRequestDto dto, User user, Movie movie) {
        return Comment.builder()
                .content(dto.getContent())
                .user(user)
                .rate(dto.getRate())
                .movie(movie)
                .build();
    }

    public CommentResponseDto toDTO(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .rate(comment.getRate())
                .userId(comment.getUser().getId())
                .movieId(comment.getMovie().getId())
                .build();
    }
}
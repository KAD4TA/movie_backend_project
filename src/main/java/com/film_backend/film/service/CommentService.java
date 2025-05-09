package com.film_backend.film.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.film_backend.film.dtos.request.CommentRequestDto;
import com.film_backend.film.dtos.response.CommentResponseDto;
import com.film_backend.film.entity.Comment;
import com.film_backend.film.entity.Movie;
import com.film_backend.film.entity.User;
import com.film_backend.film.mapper.CommentMapper;
import com.film_backend.film.repository.CommentRepository;
import com.film_backend.film.repository.MovieRepository;
import com.film_backend.film.repository.UserRepository;
import com.film_backend.film.util.JwtUtil;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final CommentMapper commentMapper;
    private final JwtUtil jwtUtil;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository,
                          MovieRepository movieRepository, CommentMapper commentMapper, JwtUtil jwtUtil) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.commentMapper = commentMapper;
        this.jwtUtil = jwtUtil;
    }

    public CommentResponseDto createComment(CommentRequestDto dto, String token) {
        String userEmail = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Comment comment = commentMapper.toEntity(dto, user, movie);
        comment = commentRepository.save(comment);

        
        List<Comment> comments = commentRepository.findByMovieId(movie.getId());
        movie.updateAverageRating(comments);
        movieRepository.save(movie);

        return commentMapper.toDTO(comment);
    }
}
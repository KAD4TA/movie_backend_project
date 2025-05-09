package com.film_backend.film.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Movie movie;
    private Comment comment;
    private CommentRequestDto commentRequestDto;
    private CommentResponseDto commentResponseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        movie = new Movie();
        movie.setId(1L);

        comment = new Comment();
        comment.setId(1L);
        comment.setUser(user);
        comment.setMovie(movie);
        comment.setRate(5);

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setMovieId(1L);
        commentRequestDto.setRate(5);
        commentRequestDto.setContent("Great movie!");

        commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(1L);
        commentResponseDto.setRate(5);
        commentResponseDto.setContent("Great movie!");
    }

    @Test
    void createComment_basariliYorumOlusturma() {
       
        String token = "validToken";
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(commentMapper.toEntity(commentRequestDto, user, movie)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentRepository.findByMovieId(1L)).thenReturn(List.of(comment));
        when(movieRepository.save(movie)).thenReturn(movie);
        when(commentMapper.toDTO(comment)).thenReturn(commentResponseDto);

        
        CommentResponseDto result = commentService.createComment(commentRequestDto, token);

        
        assertThat(result).isEqualTo(commentResponseDto);
        verify(commentRepository).save(comment);
        verify(movieRepository).save(movie);
        verify(commentMapper).toDTO(comment);
    }

    @Test
    void createComment_kullanıcıBulunamazsaHataFırlatır() {
       
        String token = "validToken";
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

      
        assertThatThrownBy(() -> commentService.createComment(commentRequestDto, token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void createComment_filmBulunamazsaHataFırlatır() {
    
        String token = "validToken";
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> commentService.createComment(commentRequestDto, token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Movie not found");
    }
}
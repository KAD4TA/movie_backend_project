package com.film_backend.film.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.film_backend.film.dtos.request.MovieRequestDto;
import com.film_backend.film.dtos.response.MovieDetailResponseDto;
import com.film_backend.film.dtos.response.MovieSimpleResponseDto;
import com.film_backend.film.entity.Movie;
import com.film_backend.film.enums.Genre;
import com.film_backend.film.mapper.MovieMapper;
import com.film_backend.film.repository.MovieRepository;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;
    private MovieRequestDto movieRequestDto;
    private MovieSimpleResponseDto movieSimpleResponseDto;
    private MovieDetailResponseDto movieDetailResponseDto;

    @BeforeEach
    void setUp() {
        // Set up Movie entity with genres as Set<Genre>
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
        movie.setDescription("A mind-bending thriller");
        movie.setGenres(new HashSet<>(Set.of(Genre.SCIFI, Genre.ACTION))); // Using Set<Genre>
        movie.setVideoUrl("http://video.url");
        movie.setPosterUrl("http://poster.url");

        // Set up MovieRequestDto with genres as List<String> (as defined in the DTO)
        movieRequestDto = new MovieRequestDto();
        movieRequestDto.setTitle("Inception");
        movieRequestDto.setDescription("A mind-bending thriller");
        movieRequestDto.setGenres(new HashSet<>(Set.of(Genre.SCIFI, Genre.ACTION))); // Genres as List<String> in DTO
        movieRequestDto.setVideoUrl("http://video.url");
        movieRequestDto.setPosterUrl("http://poster.url");

        // Set up MovieSimpleResponseDto
        movieSimpleResponseDto = new MovieSimpleResponseDto();
        movieSimpleResponseDto.setId(1L);
        movieSimpleResponseDto.setTitle("Inception");

        // Set up MovieDetailResponseDto with genres as List<String> (as defined in the DTO)
        movieDetailResponseDto = new MovieDetailResponseDto();
        movieDetailResponseDto.setId(1L);
        movieDetailResponseDto.setTitle("Inception");
        movieDetailResponseDto.setDescription("A mind-bending thriller");
        movieDetailResponseDto.setGenre(new HashSet<>(Set.of(Genre.SCIFI, Genre.ACTION)));
    }

    @Test
    void createMovie_successfulMovieCreation() throws Exception {
        // Arrange
        when(movieMapper.toEntity(movieRequestDto)).thenReturn(movie);
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.toSimpleDTO(movie)).thenReturn(movieSimpleResponseDto);

        // Act
        MovieSimpleResponseDto result = movieService.createMovie(movieRequestDto);

        // Assert
        assertThat(result).isEqualTo(movieSimpleResponseDto);
        verify(movieRepository).save(movie);
        verify(movieMapper).toSimpleDTO(movie);
    }

    @Test
    void createMovie_nullDtoThrowsError() {
        // Act & Assert
        assertThatThrownBy(() -> movieService.createMovie(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Movie data is required");
    }

    @Test
    void updateMovie_successfulUpdate() throws Exception {
        // Arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.toSimpleDTO(movie)).thenReturn(movieSimpleResponseDto);

        // Act
        MovieSimpleResponseDto result = movieService.updateMovie(1L, movieRequestDto);

        // Assert
        assertThat(result).isEqualTo(movieSimpleResponseDto);
        verify(movieRepository).save(movie);
        assertThat(movie.getTitle()).isEqualTo(movieRequestDto.getTitle());
    }

    @Test
    void updateMovie_movieNotFoundThrowsError() {
        // Arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> movieService.updateMovie(1L, movieRequestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Movie not found");
    }

    @Test
    void deleteMovie_successfulDeletion() {
        // Act
        movieService.deleteMovie(1L);

        // Assert
        verify(movieRepository).deleteById(1L);
    }

    @Test
    void listSimpleMovies_listAllMovies() {
        // Arrange
        when(movieRepository.findAll()).thenReturn(List.of(movie));
        when(movieMapper.toSimpleDTO(movie)).thenReturn(movieSimpleResponseDto);

        // Act
        List<MovieSimpleResponseDto> result = movieService.listSimpleMovies(null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(movieSimpleResponseDto);
    }

    @Test
    void listSimpleMovies_filterByTitle() {
        // Arrange
        when(movieRepository.findByTitleContainingIgnoreCase("Inception")).thenReturn(List.of(movie));
        when(movieMapper.toSimpleDTO(movie)).thenReturn(movieSimpleResponseDto);

        // Act
        List<MovieSimpleResponseDto> result = movieService.listSimpleMovies("Inception");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(movieSimpleResponseDto);
    }

    @Test
    void getMovieById_successfulDetailRetrieval() {
        // Arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieMapper.toDetailDTO(movie)).thenReturn(movieDetailResponseDto);

        // Act
        MovieDetailResponseDto result = movieService.getMovieById(1L);

        // Assert
        assertThat(result).isEqualTo(movieDetailResponseDto);
    }

    @Test
    void getMovieById_movieNotFoundThrowsError() {
        // Arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> movieService.getMovieById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Movie not found");
    }
}

package com.film_backend.film.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.film_backend.film.entity.Movie;

@Repository

public interface MovieRepository extends JpaRepository<Movie, Long> {
    
    @EntityGraph(attributePaths = "comments")
    List<Movie> findAll();

    @EntityGraph(attributePaths = "comments")
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
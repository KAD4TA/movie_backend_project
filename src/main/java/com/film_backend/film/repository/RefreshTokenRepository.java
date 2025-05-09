package com.film_backend.film.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.film_backend.film.entity.RefreshToken;
import com.film_backend.film.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByUser(User user);
    boolean existsByToken(String token);
    void deleteByToken(String token);
    void deleteByUser(User user);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
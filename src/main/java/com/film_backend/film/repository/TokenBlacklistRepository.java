package com.film_backend.film.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.film_backend.film.entity.TokenBlacklist;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, String> {
    boolean existsByToken(String token);

    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
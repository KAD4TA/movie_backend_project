package com.film_backend.film.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TokenBlacklist {

    @Id
    private String token;

    private LocalDateTime blacklistedAt;

    private LocalDateTime expiresAt;
}
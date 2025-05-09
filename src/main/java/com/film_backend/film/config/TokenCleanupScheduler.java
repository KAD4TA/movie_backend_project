package com.film_backend.film.config;

import com.film_backend.film.repository.RefreshTokenRepository;
import com.film_backend.film.repository.TokenBlacklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupScheduler.class);
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupScheduler(TokenBlacklistRepository tokenBlacklistRepository, RefreshTokenRepository refreshTokenRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        tokenBlacklistRepository.deleteByExpiresAtBefore(now);
        refreshTokenRepository.deleteByExpiresAtBefore(now);
        log.debug("Expired tokens and refresh tokens cleaned.");
    }
}
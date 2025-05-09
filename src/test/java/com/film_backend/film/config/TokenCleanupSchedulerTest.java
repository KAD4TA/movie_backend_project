package com.film_backend.film.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.film_backend.film.repository.RefreshTokenRepository;
import com.film_backend.film.repository.TokenBlacklistRepository;

class TokenCleanupSchedulerTest {

    private TokenBlacklistRepository tokenBlacklistRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private TokenCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        tokenBlacklistRepository = mock(TokenBlacklistRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        scheduler = new TokenCleanupScheduler(tokenBlacklistRepository, refreshTokenRepository);
    }

    @Test
    void cleanExpiredTokens_shouldCallDeleteMethodsWithCurrentTime() {
        // When
        scheduler.cleanExpiredTokens();

        // Then
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(tokenBlacklistRepository, times(1)).deleteByExpiresAtBefore(timeCaptor.capture());
        verify(refreshTokenRepository, times(1)).deleteByExpiresAtBefore(timeCaptor.capture());

        LocalDateTime usedTime = timeCaptor.getAllValues().get(0);
        assertNotNull(usedTime);
        assertTrue(usedTime.isBefore(LocalDateTime.now().plusSeconds(5)),
                "Scheduled deletion should use current time (UTC)");
    }
}

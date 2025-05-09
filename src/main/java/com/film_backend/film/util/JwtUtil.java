package com.film_backend.film.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.film_backend.film.entity.TokenBlacklist;
import com.film_backend.film.entity.User;
import com.film_backend.film.repository.TokenBlacklistRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component
@Data
@RequiredArgsConstructor
public class JwtUtil {

    private static final ZoneId ZONE_ID = ZoneId.of("UTC");

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    private Key getSigningKey() {
        try {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT secret key", e);
        }
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        return createToken(claims, user.getEmail(), expiration);
    }

    public String generateRefreshToken(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        return createToken(claims, user.getEmail(), refreshExpiration);
    }

    public Long extractId(String token) {
        Claims claims = extractClaims(token);
        Object id = claims.get("id");
        if (id == null) {
            throw new IllegalArgumentException("User ID not found in token");
        }
        try {
            return Long.valueOf(id.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format in token");
        }
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return extractId(token);
    }

    public String extractRole(String token) {
        Object role = extractClaims(token).get("role");
        if (role == null) {
            throw new IllegalArgumentException("Role not found in token");
        }
        return role.toString();
    }

    public boolean isTokenValid(String token, String email) {
        try {
            if (tokenBlacklistRepository.existsByToken(token)) {
                return false;
            }
            final String tokenEmail = extractEmail(token);
            return tokenEmail.equals(email) && !isTokenExpired(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public void blacklistToken(String token) {
        if (tokenBlacklistRepository.existsByToken(token)) {
            return;
        }
        try {
            TokenBlacklist blacklistEntry = new TokenBlacklist();
            blacklistEntry.setToken(token);
            blacklistEntry.setBlacklistedAt(LocalDateTime.now(ZONE_ID));
            blacklistEntry.setExpiresAt(getExpirationDateFromToken(token));
            tokenBlacklistRepository.save(blacklistEntry);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to blacklist token: " + e.getMessage(), e);
        }
    }

    public LocalDateTime getExpirationDateFromToken(String token) {
        try {
            Date expirationDate = extractClaims(token).getExpiration();
            return expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token: Unable to retrieve expiration date");
        }
    }
}
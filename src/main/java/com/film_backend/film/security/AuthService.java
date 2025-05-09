package com.film_backend.film.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.film_backend.film.dtos.request.LoginRequestDto;
import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.AuthResponseDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.entity.TokenBlacklist;
import com.film_backend.film.entity.User;
import com.film_backend.film.enums.Role;
import com.film_backend.film.mapper.UserMapper;
import com.film_backend.film.repository.TokenBlacklistRepository;
import com.film_backend.film.repository.UserRepository;
import com.film_backend.film.util.ImageUtil;
import com.film_backend.film.util.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    private final UserMapper userMapper;
    private final ImageUtil imageUtil;
    private final String defaultProfilePicture;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            UserMapper userMapper,
            ImageUtil imageUtil,
            TokenBlacklistRepository tokenBlacklistRepository,
            @Value("${app.default-profile-picture:/default.png}") String defaultProfilePicture) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.imageUtil = imageUtil;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.defaultProfilePicture = defaultProfilePicture;
    }

    public UserResponseDto registerUser(UserRequestDto dto) {
        return register(dto, Role.USER);
    }

    public UserResponseDto registerAdmin(UserRequestDto dto) {
        return register(dto, Role.ADMIN);
    }

    private UserResponseDto register(UserRequestDto dto, Role role) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use.");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);

        user.setProfilePicture(processProfileImage(dto.getProfileImage(), user.getUsername(), user.getProfilePicture()));

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    private String processProfileImage(String profileImage, String username, String currentProfilePicture) {
        if (profileImage == null || profileImage.isEmpty()) {
            return defaultProfilePicture;
        }

        try {
            
            if (currentProfilePicture != null && !currentProfilePicture.equals(defaultProfilePicture)) {
                Files.deleteIfExists(Paths.get(currentProfilePicture));
            }
            return imageUtil.saveImage(profileImage, username);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to process profile image: " + e.getMessage(), e);
        }
    }

    public AuthResponseDto login(LoginRequestDto dto) {
        try {
            User user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));

            String token = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            return AuthResponseDto.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .user(userMapper.toDTO(user))
                    .build();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Login failed: " + e.getMessage(), e);
        }
    }

    public AuthResponseDto refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required.");
        }

        String email;
        try {
            email = jwtUtil.extractEmail(refreshToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token: " + e.getMessage(), e);
        }

        if (!jwtUtil.isTokenValid(refreshToken, email)) {
            throw new IllegalArgumentException("Refresh token is invalid or expired.");
        }

        Long userId;
        try {
            userId = jwtUtil.getUserIdFromToken(refreshToken);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("User ID not found in refresh token: " + e.getMessage(), e);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String newToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponseDto.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .user(userMapper.toDTO(user))
                .build();
    }

    public void logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required.");
        }

        String email;
        try {
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token: " + e.getMessage(), e);
        }

        if (!jwtUtil.isTokenValid(token, email)) {
            throw new IllegalArgumentException("Token is invalid or expired.");
        }

        LocalDateTime expiresAt = jwtUtil.getExpirationDateFromToken(token);

        TokenBlacklist blacklistedToken = new TokenBlacklist();
        blacklistedToken.setToken(token);
        blacklistedToken.setBlacklistedAt(LocalDateTime.now());
        blacklistedToken.setExpiresAt(expiresAt);

        tokenBlacklistRepository.save(blacklistedToken);
    }
}
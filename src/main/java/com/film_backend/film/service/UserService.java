package com.film_backend.film.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.entity.RefreshToken;
import com.film_backend.film.entity.User;
import com.film_backend.film.mapper.UserMapper;
import com.film_backend.film.repository.RefreshTokenRepository;
import com.film_backend.film.repository.UserRepository;
import com.film_backend.film.util.ImageUtil;
import com.film_backend.film.util.JwtUtil;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ImageUtil imageUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final String DEFAULT_PROFILE_PICTURE = "/default.png";

    public UserService(UserRepository userRepository, UserMapper userMapper, JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder, ImageUtil imageUtil, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.imageUtil = imageUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public UserResponseDto updateProfile(UserRequestDto dto, String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required.");
        }

        Long userId;
        try {
            userId = jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token: " + e.getMessage(), e);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            user.setEmail(dto.getEmail());
        }

        
        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            try {
                if (user.getProfilePicture() != null && !user.getProfilePicture().equals(DEFAULT_PROFILE_PICTURE)) {
                    Files.deleteIfExists(Paths.get(user.getProfilePicture()));
                }
                String filePath = imageUtil.saveImage(dto.getProfileImage(), user.getUsername());
                user.setProfilePicture(filePath);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to save profile image: " + e.getMessage(), e);
            }
        } else {
            user.setProfilePicture(DEFAULT_PROFILE_PICTURE);
        }

        // Override tokens if there is a password change
        boolean passwordChanged = false;
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            passwordChanged = true;
        }

        user = userRepository.save(user);

        // Add all tokens to blacklist if password changed
        if (passwordChanged) {
            jwtUtil.blacklistToken(token); 
            List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);
            for (RefreshToken refreshToken : refreshTokens) {
                jwtUtil.blacklistToken(refreshToken.getToken());
                refreshTokenRepository.deleteByToken(refreshToken.getToken());
            }
        }

        return userMapper.toDTO(user);
    }

    public void deleteProfile(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required.");
        }

        Long userId;
        try {
            userId = jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token: " + e.getMessage(), e);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        
        if (user.getProfilePicture() != null && !user.getProfilePicture().equals(DEFAULT_PROFILE_PICTURE)) {
            try {
                Files.deleteIfExists(Paths.get(user.getProfilePicture()));
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to delete profile image: " + e.getMessage(), e);
            }
        }

        // Add the access token and all refresh tokens to the blacklist
        jwtUtil.blacklistToken(token);
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);
        for (RefreshToken refreshToken : refreshTokens) {
            jwtUtil.blacklistToken(refreshToken.getToken());
            refreshTokenRepository.deleteByToken(refreshToken.getToken());
        }

        userRepository.deleteById(userId);
    }

    public void logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required.");
        }

        Long userId;
        try {
            userId = jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token: " + e.getMessage(), e);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Add the access token and all refresh tokens to the blacklist
        jwtUtil.blacklistToken(token);
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);
        for (RefreshToken refreshToken : refreshTokens) {
            jwtUtil.blacklistToken(refreshToken.getToken());
            refreshTokenRepository.deleteByToken(refreshToken.getToken());
        }
    }
}
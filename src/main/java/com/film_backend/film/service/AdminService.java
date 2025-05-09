package com.film_backend.film.service;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.entity.RefreshToken;
import com.film_backend.film.entity.User;
import com.film_backend.film.exception.ResourceNotFoundException;
import com.film_backend.film.mapper.UserMapper;
import com.film_backend.film.repository.RefreshTokenRepository;
import com.film_backend.film.repository.UserRepository;
import com.film_backend.film.util.ImageUtil;
import com.film_backend.film.util.JwtUtil;

import lombok.RequiredArgsConstructor;

/**
 * Service class for managing admin-related operations such as updating profiles, uploading images, and managing users.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final ImageUtil imageUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Updates an admin's details based on the provided token and request DTO.
     *
     * @param token JWT token for authentication.
     * @param dto   User request DTO containing updated details.
     * @return Updated user details as a DTO.
     * @throws IOException If image processing fails.
     * @throws IllegalArgumentException If the token is invalid or user is not found.
     */
    public UserResponseDto updateAdmin(String token, UserRequestDto dto) throws IOException {
        if (!jwtUtil.isTokenValid(token, jwtUtil.extractEmail(token))) {
            
            throw new IllegalArgumentException("Invalid token.");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    
                    return new ResourceNotFoundException("User not found.");
                });

        if (StringUtils.isNotBlank(dto.getUsername())) {
            user.setUsername(dto.getUsername());
        }
        if (StringUtils.isNotBlank(dto.getEmail())) {
            user.setEmail(dto.getEmail());
        }
        boolean passwordChanged = false;
        if (StringUtils.isNotBlank(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            passwordChanged = true;
        }
        if (StringUtils.isNotBlank(dto.getProfileImage())) {
            String savedImagePath = imageUtil.saveImage(dto.getProfileImage(), user.getUsername());
            user.setProfilePicture(savedImagePath);
        }

        user = userRepository.save(user);

        // Add all tokens to blacklist if password changed
        if (passwordChanged) {
            try {
                jwtUtil.blacklistToken(token); 
                List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);
                for (RefreshToken refreshToken : refreshTokens) {
                    jwtUtil.blacklistToken(refreshToken.getToken());
                    refreshTokenRepository.deleteByToken(refreshToken.getToken());
                }
                
            } catch (Exception e) {
                
                throw new RuntimeException("Token blacklisting failed.");
            }
        }

        return userMapper.toDTO(user);
    }

    /**
     * Uploads a profile picture for the admin identified by the token.
     *
     * @param token JWT token for authentication.
     * @param dto   User request DTO containing the profile image.
     * @return Updated user details as a DTO.
     * @throws IOException If image processing fails.
     * @throws IllegalArgumentException If the token is invalid, user is not found, or profile image is missing.
     */
    public UserResponseDto uploadProfilePicture(String token, UserRequestDto dto) throws IOException {
        if (!jwtUtil.isTokenValid(token, jwtUtil.extractEmail(token))) {
            
            throw new IllegalArgumentException("Invalid token.");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    
                    return new ResourceNotFoundException("User not found.");
                });

        if (StringUtils.isBlank(dto.getProfileImage())) {
            
            throw new IllegalArgumentException("Profile image is required.");
        }

        String savedImagePath = imageUtil.saveImage(dto.getProfileImage(), user.getUsername());
        user.setProfilePicture(savedImagePath);

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    /**
     * Retrieves all users with pagination support.
     *
     * @param pageable Pagination information.
     * @return List of user DTOs.
     */
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id User ID.
     * @return User details as a DTO.
     * @throws ResourceNotFoundException If the user is not found.
     */
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
        return userMapper.toDTO(user);
    }

    /**
     * Deletes an admin account identified by the token.
     *
     * @param token JWT token for authentication.
     * @throws ResourceNotFoundException If the admin is not found.
     * @throws IllegalStateException If the last admin cannot be deleted.
     */
    public void deleteAdmin(String token) {
        if (!jwtUtil.isTokenValid(token, jwtUtil.extractEmail(token))) {
            
            throw new IllegalArgumentException("Invalid token.");
        }

        Long adminId = jwtUtil.extractId(token);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> {
                    
                    return new ResourceNotFoundException("Admin not found.");
                });

        long adminCount = userRepository.countByRole("ADMIN"); 
        if (adminCount <= 1) {
            
            throw new IllegalStateException("Cannot delete the last admin.");
        }

       
        try {
            jwtUtil.blacklistToken(token);
            List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(admin);
            for (RefreshToken refreshToken : refreshTokens) {
                jwtUtil.blacklistToken(refreshToken.getToken());
                refreshTokenRepository.deleteByToken(refreshToken.getToken());
            }
            
        } catch (Exception e) {
            
            throw new RuntimeException("Token blacklisting failed.");
        }

        userRepository.delete(admin);
        
    }

   
}
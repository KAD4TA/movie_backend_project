package com.film_backend.film.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.entity.User;
import com.film_backend.film.mapper.UserMapper;
import com.film_backend.film.repository.RefreshTokenRepository;
import com.film_backend.film.repository.UserRepository;
import com.film_backend.film.util.ImageUtil;
import com.film_backend.film.util.JwtUtil;

/**
 * Unit tests for UserService, covering profile updates, deletion, and logout.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImageUtil imageUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private String token;
    private Long userId;
    private static final String DEFAULT_PROFILE_PICTURE = "/default.png";

    @BeforeEach
    void setUp() {
        token = "valid-token";
        userId = 1L;

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setUsername("testUser");
        user.setPassword("encoded-password");
        user.setProfilePicture(DEFAULT_PROFILE_PICTURE);

        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("newUser");
        userRequestDto.setEmail("new@example.com");
        userRequestDto.setPassword("newPassword");
        userRequestDto.setProfileImage("https://example.com/image.jpg");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(userId);
        userResponseDto.setUsername("newUser");
        userResponseDto.setEmail("new@example.com");
    }

    @Test
    void updateProfile_ValidTokenAndData_ReturnsUpdatedUser() throws IOException {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded-new-password");
        when(imageUtil.saveImage(anyString(), anyString())).thenReturn("/new/image.jpg");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDto);
        when(refreshTokenRepository.findByUser(user)).thenReturn(Collections.emptyList());

        // Act
        UserResponseDto result = userService.updateProfile(userRequestDto, token);

        // Assert
        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userRepository).save(user);
        verify(jwtUtil).blacklistToken(token);
        verify(refreshTokenRepository).findByUser(user);
        verify(imageUtil).saveImage(userRequestDto.getProfileImage(), user.getUsername());
    }

    @Test
    void updateProfile_NullToken_ThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(userRequestDto, null));
        assertEquals("Token is required.", exception.getMessage());
        verify(jwtUtil, never()).getUserIdFromToken(anyString());
        verify(userRepository, never()).save(any()); // Fixed syntax error
    }

    @Test
    void updateProfile_InvalidToken_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenThrow(new IllegalArgumentException("Invalid token."));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(userRequestDto, token));
        assertEquals("Invalid token: Invalid token.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_UserNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(userRequestDto, token));
        assertEquals("User not found.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_ImageSaveFails_ThrowsIllegalArgumentException() throws IOException {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(imageUtil.saveImage(anyString(), anyString())).thenThrow(new IOException("Disk error"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(userRequestDto, token));
        assertEquals("Failed to save profile image: Disk error", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_NoProfileImage_SetsDefaultPicture() throws IOException {
        // Arrange
        userRequestDto.setProfileImage(null);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDto);

        // Act
        UserResponseDto result = userService.updateProfile(userRequestDto, token);

        // Assert
        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userRepository).save(user);
        verify(imageUtil, never()).saveImage(anyString(), anyString());
        assertEquals(DEFAULT_PROFILE_PICTURE, user.getProfilePicture());
    }

    @Test
    void deleteProfile_ValidToken_DeletesUser() throws IOException {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Collections.emptyList());
        doNothing().when(userRepository).deleteById(userId);

        // Act
        userService.deleteProfile(token);

        // Assert
        verify(userRepository).deleteById(userId);
        verify(jwtUtil).blacklistToken(token);
        verify(refreshTokenRepository).findByUser(user);
        verifyNoInteractions(imageUtil); // Default picture, no deletion
    }

    @Test
    void deleteProfile_NullToken_ThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteProfile(null));
        assertEquals("Token is required.", exception.getMessage());
        verify(jwtUtil, never()).getUserIdFromToken(anyString());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProfile_InvalidToken_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenThrow(new IllegalArgumentException("Invalid token."));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteProfile(token));
        assertEquals("Invalid token: Invalid token.", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProfile_UserNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteProfile(token));
        assertEquals("User not found.", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void logout_ValidToken_BlacklistsTokens() {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Collections.emptyList());

        // Act
        userService.logout(token);

        // Assert
        verify(jwtUtil).blacklistToken(token);
        verify(refreshTokenRepository).findByUser(user);
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository); // No deletion or save
    }

    @Test
    void logout_NullToken_ThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.logout(null));
        assertEquals("Token is required.", exception.getMessage());
        verify(jwtUtil, never()).getUserIdFromToken(anyString());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void logout_InvalidToken_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenThrow(new IllegalArgumentException("Invalid token."));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.logout(token));
        assertEquals("Invalid token: Invalid token.", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void logout_UserNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.logout(token));
        assertEquals("User not found.", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }
}
package com.film_backend.film.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.entity.User;
import com.film_backend.film.exception.ResourceNotFoundException;
import com.film_backend.film.mapper.UserMapper;
import com.film_backend.film.repository.RefreshTokenRepository;
import com.film_backend.film.repository.UserRepository;
import com.film_backend.film.util.ImageUtil;
import com.film_backend.film.util.JwtUtil;

/**
 * Unit tests for AdminService, covering all admin-related operations.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ImageUtil imageUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private String token;
    private String email;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        email = "admin@example.com";
        token = "valid-token";
        user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setProfilePicture("/images/admin.jpg");

        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("newAdmin");
        userRequestDto.setEmail("newadmin@example.com");
        userRequestDto.setPassword("newPassword");
        userRequestDto.setProfileImage("https://example.com/image.jpg");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setUsername("newAdmin");
        userResponseDto.setEmail("newadmin@example.com");
    }

    /**
     * Tests successful admin update with valid token and data.
     */
    @Test
    void updateAdmin_ValidTokenAndData_ReturnsUpdatedUser() throws IOException {
        // Arrange
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded-new-password");
        when(imageUtil.saveImage(anyString(), anyString())).thenReturn("/new/image.jpg");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDto);

        // Act
        UserResponseDto result = adminService.updateAdmin(token, userRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userRepository).save(user);
        verify(jwtUtil).blacklistToken(token);
        verify(refreshTokenRepository).findByUser(user);
    }

    /**
     * Tests admin update with invalid token.
     */
    @Test
    void updateAdmin_InvalidToken_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> adminService.updateAdmin(token, userRequestDto));
        verify(userRepository, never()).save(any());
    }

    /**
     * Tests admin update when user is not found.
     */
    @Test
    void updateAdmin_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> adminService.updateAdmin(token, userRequestDto));
        verify(userRepository, never()).save(any());
    }

    /**
     * Tests successful profile picture upload with valid token and image.
     */
    @Test
    void uploadProfilePicture_ValidTokenAndImage_ReturnsUpdatedUser() throws IOException {
        // Arrange
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(imageUtil.saveImage(anyString(), anyString())).thenReturn("/new/image.jpg");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDto);

        // Act
        UserResponseDto result = adminService.uploadProfilePicture(token, userRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userRepository).save(user);
        verify(imageUtil).saveImage(userRequestDto.getProfileImage(), user.getUsername());
    }

    /**
     * Tests profile picture upload with missing image.
     */
    @Test
    void uploadProfilePicture_MissingImage_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        userRequestDto.setProfileImage(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> adminService.uploadProfilePicture(token, userRequestDto));
        verify(userRepository, never()).save(any());
    }

    /**
     * Tests retrieval of all users with pagination.
     */
    @Test
    void getAllUsers_ReturnsPagedUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDTO(user)).thenReturn(userResponseDto);

        // Act
        Page<UserResponseDto> result = adminService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(userResponseDto, result.getContent().get(0));
        verify(userRepository).findAll(pageable);
    }

    /**
     * Tests retrieval of a user by ID when the user exists.
     */
    @Test
    void getUserById_UserExists_ReturnsUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDto);

        // Act
        UserResponseDto result = adminService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(userResponseDto, result);
        verify(userRepository).findById(1L);
    }

    /**
     * Tests retrieval of a user by ID when the user is not found.
     */
    @Test
    void getUserById_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> adminService.getUserById(1L));
        verify(userRepository).findById(1L);
    }

    /**
     * Tests successful admin deletion with valid token and multiple admins.
     */
    @Test
    void deleteAdmin_ValidTokenAndMultipleAdmins_DeletesAdmin() {
        // Arrange
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(true);
        when(jwtUtil.extractId(token)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countByRole("ADMIN")).thenReturn(2L);
        when(refreshTokenRepository.findByUser(user)).thenReturn(List.of());

        // Act
        adminService.deleteAdmin(token);

        // Assert
        verify(userRepository).delete(user);
        verify(jwtUtil).blacklistToken(token);
        verify(refreshTokenRepository).findByUser(user);
    }

    /**
     * Tests admin deletion when the user is the last admin.
     */
    @Test
    void deleteAdmin_LastAdmin_ThrowsIllegalStateException() {
        // Arrange
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(true);
        when(jwtUtil.extractId(token)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countByRole("ADMIN")).thenReturn(1L);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> adminService.deleteAdmin(token));
        verify(userRepository, never()).delete(any());
        verify(userRepository).findById(1L);
    }

    /**
     * Tests admin deletion with an invalid token.
     */
    @Test
    void deleteAdmin_InvalidToken_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> adminService.deleteAdmin(token));
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).delete(any());
    }
}
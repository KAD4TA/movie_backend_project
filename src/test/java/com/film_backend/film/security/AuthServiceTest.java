package com.film_backend.film.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.film_backend.film.dtos.request.LoginRequestDto;
import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.AuthResponseDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.entity.TokenBlacklist;
import com.film_backend.film.entity.User;
import com.film_backend.film.mapper.UserMapper;
import com.film_backend.film.repository.TokenBlacklistRepository;
import com.film_backend.film.repository.UserRepository;
import com.film_backend.film.util.ImageUtil;
import com.film_backend.film.util.JwtUtil;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ImageUtil imageUtil;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtUtil,
                mock(AuthenticationManager.class),
                userMapper,
                imageUtil,
                tokenBlacklistRepository,
                "/default.png"
        );
    }

    @Test
    void registerUser_ShouldReturnUserResponseDto() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("test@example.com");
        userRequestDto.setPassword("password");
        userRequestDto.setUsername("testUser");

        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testUser");

        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());
        when(userMapper.toEntity(any(UserRequestDto.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(any(User.class))).thenReturn(new UserResponseDto());

        UserResponseDto response = authService.registerUser(userRequestDto);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailExists() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("test@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> authService.registerUser(userRequestDto));
    }

    @Test
    void login_ShouldReturnAuthResponseDto() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password");

        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testUser");

        String token = "token";
        String refreshToken = "refreshToken";

        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(user));
        when(jwtUtil.generateToken(any(User.class))).thenReturn(token);
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn(refreshToken);
        when(userMapper.toDTO(any(User.class))).thenReturn(new UserResponseDto());

        AuthResponseDto response = authService.login(loginRequestDto);

        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequestDto));
    }

    @Test
    void refreshToken_ShouldReturnNewTokens() {
        String refreshToken = "refreshToken";
        String newToken = "newToken";
        String newRefreshToken = "newRefreshToken";

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(jwtUtil.isTokenValid(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.of(user));
        when(jwtUtil.generateToken(any(User.class))).thenReturn(newToken);
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn(newRefreshToken);
        when(userMapper.toDTO(any(User.class))).thenReturn(new UserResponseDto());

        AuthResponseDto response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals(newToken, response.getToken());
        assertEquals(newRefreshToken, response.getRefreshToken());
    }

    @Test
    void logout_ShouldBlacklistToken() {
        String token = "token";

        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(jwtUtil.isTokenValid(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.getExpirationDateFromToken(anyString())).thenReturn(java.time.LocalDateTime.now().plusHours(1));

        authService.logout(token);

        verify(tokenBlacklistRepository, times(1)).save(any(TokenBlacklist.class));
    }
}

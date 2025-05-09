package com.film_backend.film.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.film_backend.film.entity.User;
import com.film_backend.film.enums.Role;
import com.film_backend.film.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomerUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        // Test için örnek kullanıcı oluştur
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
    }

    @Test
    void loadUserByUsername_kullanıcıBulunursaUserDetailsDöner() {
        // Düzenle: userRepository.findByEmail doğru kullanıcıyı dönecek
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Eylem: loadUserByUsername metodunu çağır
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Doğrula: Dönen UserDetails nesnesi doğru bilgiler içeriyor mu?
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("USER");
    }

    @Test
    void loadUserByUsername_kullanıcıBulunamazsaHataFırlatır() {
        // Düzenle: userRepository.findByEmail boş bir Optional dönecek
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Eylem & Doğrula: loadUserByUsername çağrısı UsernameNotFoundException fırlatmalı
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: nonexistent@example.com");
    }
}
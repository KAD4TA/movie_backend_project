package com.film_backend.film.mapper;

import org.springframework.stereotype.Component;

import com.film_backend.film.dtos.request.UserRequestDto;
import com.film_backend.film.dtos.response.UserResponseDto;
import com.film_backend.film.entity.User;

@Component
public class UserMapper {

    public User toEntity(UserRequestDto dto) {
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .build();
    }

    public UserResponseDto toDTO(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}
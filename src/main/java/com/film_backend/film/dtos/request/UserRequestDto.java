package com.film_backend.film.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {
    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Pattern(regexp = "^(https?://.*\\.(?:jpg|jpeg|png)|/.*\\.(?:jpg|jpeg|png)|[a-zA-Z]:\\\\.*\\.(?:jpg|jpeg|png))$",
             message = "Provide a valid JPG/PNG URL or local file path (e.g., https://example.com/image.jpg, /path/to/image.jpg, or C:\\path\\to\\image.jpg).")
    private String profileImage;
}
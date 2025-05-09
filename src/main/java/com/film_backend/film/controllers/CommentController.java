package com.film_backend.film.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.film_backend.film.dtos.request.CommentRequestDto;
import com.film_backend.film.dtos.response.CommentResponseDto;
import com.film_backend.film.service.CommentService;
import com.film_backend.film.util.TokenUtils;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<?> createComment(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody CommentRequestDto dto) {
        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(403).body("Forbidden: No token provided");
            }
            String extractedToken = TokenUtils.extractToken(token);
            CommentResponseDto response = commentService.createComment(dto, extractedToken);
            return ResponseEntity.ok(response);
        } catch (MalformedJwtException e) {
            return ResponseEntity.badRequest().body("Invalid JWT token: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

}

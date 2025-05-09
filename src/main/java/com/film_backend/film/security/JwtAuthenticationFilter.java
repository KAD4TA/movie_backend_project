package com.film_backend.film.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.film_backend.film.exception.UnauthorizedException;
import com.film_backend.film.repository.TokenBlacklistRepository;
import com.film_backend.film.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    // Public endpoints list
    private static final Set<String> PUBLIC_ROUTES = Set.of(
        "/api/auth/login",
        "/api/auth/register/**",
        "/api/movies/list",
        "/api/movies/{id}"
    );

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService,
            TokenBlacklistRepository tokenBlacklistRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // If the request is to one of the public routes, skip the JWT check
        if (isPublicRoute(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (header == null) {
            sendErrorResponse(response, new UnauthorizedException("Authorization header is missing."), HttpStatus.UNAUTHORIZED);
            return;
        }

        if (!header.startsWith("Bearer ")) {
            sendErrorResponse(response, new UnauthorizedException("Invalid Authorization header format."), HttpStatus.UNAUTHORIZED);
            return;
        }

        token = header.substring(7);

        if (tokenBlacklistRepository.existsByToken(token)) {
            sendErrorResponse(response, new UnauthorizedException("Token is blacklisted."), HttpStatus.UNAUTHORIZED);
            return;
        }

        try {
            email = jwtUtil.extractEmail(token);
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, new UnauthorizedException("Token has expired."), HttpStatus.UNAUTHORIZED);
            return;
        } catch (MalformedJwtException e) {
            sendErrorResponse(response, new UnauthorizedException("Malformed JWT token."), HttpStatus.UNAUTHORIZED);
            return;
        } catch (Exception e) {
            sendErrorResponse(response, new UnauthorizedException("Invalid token."), HttpStatus.UNAUTHORIZED);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.isTokenValid(token, email)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                sendErrorResponse(response, new UnauthorizedException("Invalid token."), HttpStatus.UNAUTHORIZED);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicRoute(String requestURI) {
        return PUBLIC_ROUTES.stream().anyMatch(publicRoute -> {
            if (publicRoute.endsWith("/**")) {
                
                return requestURI.startsWith(publicRoute.substring(0, publicRoute.length() - 3));
            }
            return requestURI.equals(publicRoute);
        });
    }

    private void sendErrorResponse(HttpServletResponse response, UnauthorizedException ex, HttpStatus status)
            throws IOException {
        if (response.isCommitted()) return;

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(String.format("{\"error\": \"%s\"}", ex.getMessage()));
        }
    }
}

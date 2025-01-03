package com.cungthinh.authservices.helper;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cungthinh.authservices.service.JWTService;
import com.cungthinh.authservices.service.impl.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.lang.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JWTService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login");
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String jwt;
        final Long userId;

        if (isInvalidAuthorizationHeader(authorizationHeader)) {
            handleInvalidAuthorizationHeader(response, request.getRequestURI());
            return;
        }

        jwt = authorizationHeader.substring(BEARER_PREFIX.length());
        try {
            userId = Long.parseLong(jwtService.extractUserId(jwt));
            UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

            logger.info("Extracted user details: " + userDetails);

            if (jwtService.validateToken(jwt, userId)) {
                // Set the authentication in the security context
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.info("Invalid token");
                handleInvalidToken(response, request.getRequestURI());
            }
        } catch (JwtException | IllegalArgumentException e) {
            logger.info("Invalid token here");
            handleInvalidToken(response, request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isInvalidAuthorizationHeader(String authorizationHeader) {
        return authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX);
    }

    private void handleInvalidAuthorizationHeader(HttpServletResponse response, String requestURI) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(HttpServletResponse.SC_UNAUTHORIZED,
                "Token bị thiếu hoặc không hợp lệ", java.time.LocalDateTime.now().toString(), requestURI);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse)); // Convert to JSON
        return;
    }

    private void handleInvalidToken(HttpServletResponse response, String requestURI) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(HttpServletResponse.SC_UNAUTHORIZED,
                "Token không hợp lệ", java.time.LocalDateTime.now().toString(), requestURI);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse)); // Convert to JSON
        return;
    }

    @Data
    @AllArgsConstructor
    public class ErrorResponse {
        private int statusCode;
        private String message;
        private String timestamp;
        private String path;
    }
}

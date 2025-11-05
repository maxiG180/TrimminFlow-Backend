package com.trimminflow.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 *
 * This filter intercepts every HTTP request and:
 * 1. Extracts the JWT token from the Authorization header
 * 2. Validates the token
 * 3. Sets the authentication in Spring Security context
 *
 * This allows Spring Security to know which user is making the request
 * and protect routes based on authentication status and roles.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Get Authorization header from request
        final String authHeader = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token found, continue to next filter
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract token (remove "Bearer " prefix)
            final String jwt = authHeader.substring(7);

            // Extract user information from token
            final String userEmail = jwtUtil.extractEmail(jwt);
            final String role = jwtUtil.extractRole(jwt);

            // If email is valid and user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validate the token
                if (jwtUtil.validateToken(jwt, userEmail)) {

                    // Create authentication token with user's role
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    // Set additional details from the request
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the authentication in the security context
                    // This tells Spring Security that this user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // If token is invalid or expired, do nothing
            // User will be treated as unauthenticated
            logger.error("JWT authentication failed: " + e.getMessage());
        }

        // Continue to the next filter
        filterChain.doFilter(request, response);
    }
}

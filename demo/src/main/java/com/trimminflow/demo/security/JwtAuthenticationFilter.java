package com.trimminflow.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
 * 1. Extracts the JWT token from httpOnly cookie OR Authorization header (fallback)
 * 2. Validates the token
 * 3. Sets the authentication in Spring Security context
 *
 * This allows Spring Security to know which user is making the request
 * and protect routes based on authentication status and roles.
 *
 * SECURITY: Prioritizes httpOnly cookies (XSS-protected) over Authorization header
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

        String jwt = null;

        // 1. Try to get token from httpOnly cookie (SECURE - preferred method)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // 2. Fallback: Get token from Authorization header (for backward compatibility)
        if (jwt == null) {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        // If no token found, continue to next filter
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

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

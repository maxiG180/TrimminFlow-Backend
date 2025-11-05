package com.trimminflow.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT Utility class for token generation and validation
 *
 * This class handles:
 * - Generating JWT tokens for authenticated users
 * - Validating JWT tokens
 * - Extracting information from JWT tokens (email, expiration, etc.)
 */
@Component
public class JwtUtil {

    // Inject JWT secret from application.properties
    @Value("${jwt.secret}")
    private String secret;

    // Inject JWT expiration time from application.properties (in milliseconds)
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Generate a secret key from the configured secret string
     * This key is used to sign and verify JWT tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate JWT token for a user
     *
     * @param email User's email (used as the subject)
     * @param userId User's UUID
     * @param role User's role (ADMIN, BARBER, CUSTOMER)
     * @return JWT token string
     */
    public String generateToken(String email, UUID userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("role", role);
        return createToken(claims, email);
    }

    /**
     * Create the actual JWT token with claims
     *
     * @param claims Additional data to store in the token
     * @param subject The subject of the token (user's email)
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)                          // Add custom claims (userId, role)
                .subject(subject)                        // Set subject (email)
                .issuedAt(now)                          // Set token creation time
                .expiration(expiryDate)                 // Set token expiration time
                .signWith(getSigningKey())              // Sign with secret key
                .compact();                             // Build the token
    }

    /**
     * Extract the email (subject) from the token
     *
     * @param token JWT token
     * @return User's email
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the user ID from the token
     *
     * @param token JWT token
     * @return User's UUID
     */
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(userIdStr);
    }

    /**
     * Extract the user role from the token
     *
     * @param token JWT token
     * @return User's role as string
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract the expiration date from the token
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim from the token
     *
     * @param token JWT token
     * @param claimsResolver Function to extract specific claim
     * @param <T> Type of the claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from the token
     *
     * @param token JWT token
     * @return All claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())            // Verify signature with secret key
                .build()
                .parseSignedClaims(token)               // Parse the token
                .getPayload();                          // Get the claims
    }

    /**
     * Check if the token is expired
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate the token
     *
     * Checks:
     * 1. Token is not expired
     * 2. Email in token matches the provided email
     *
     * @param token JWT token
     * @param email User's email to validate against
     * @return true if valid, false otherwise
     */
    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }
}

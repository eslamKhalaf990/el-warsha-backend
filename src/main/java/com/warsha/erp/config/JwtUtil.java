package com.warsha.erp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String secretKeyString) {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // Formatter for consistent log timestamps
    private final DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private String getTimestamp() {
        return LocalDateTime.now().format(logFormat);
    }

    public String generateToken(String username, String role) {
        // 8 hours
        long EXPIRATION = 1000 * 60 * 60 * 8;
        Date expDate = new Date(System.currentTimeMillis() + EXPIRATION);

        System.out.println("[" + getTimestamp() + "] INFO: Generating JWT for " + username + " (Role: " + role + ")");
        System.out.println("[" + getTimestamp() + "] INFO: Token will expire at: " + expDate);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .claim("role", role)
                .setExpiration(expDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("[" + getTimestamp() + "] ERROR: Failed to parse JWT claims: " + e.getMessage());
            throw e;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        boolean expired = expiration.before(new Date());
        if (expired) {
            System.out.println("[" + getTimestamp() + "] WARN: Token expired at " + expiration);
        }
        return expired;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean usernameMatches = username.equals(userDetails.getUsername());
        boolean expired = isTokenExpired(token);

        if (!usernameMatches) {
            System.out.println("[" + getTimestamp() + "] ERROR: Token username '" + username + "' does not match UserDetails '" + userDetails.getUsername() + "'");
        }

        boolean isValid = (usernameMatches && !expired);
        if (isValid) {
            System.out.println("[" + getTimestamp() + "] SUCCESS: Token validation passed for " + username);
        }

        return isValid;
    }
}
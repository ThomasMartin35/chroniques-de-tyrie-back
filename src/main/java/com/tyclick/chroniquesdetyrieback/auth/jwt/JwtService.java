package com.tyclick.chroniquesdetyrieback.auth.jwt;

import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expirationTime;

    /**
     * Generates a JWT token for the given user details.
     * @param userDetails The user details for which the token is to be generated.
     * @return The generated JWT token as a String.
     */
    public String generateToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getUser().getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationTime)))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts all claims from the provided JWT token.
     * @param token The JWT token from which to extract claims.
     * @return The claims extracted from the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the username (subject) from the provided JWT token.
     * @param token The JWT token from which to extract the username.
     * @return The username (subject) extracted from the token.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the expiration date from the provided JWT token.
     * @param token The JWT token from which to extract the expiration date.
     * @return The expiration date extracted from the token.
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Checks if the provided JWT token is expired.
     * @param token The JWT token to check for expiration.
     * @return true if the token is expired, false otherwise.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates the provided JWT token against the given user details.
     * @param token The JWT token to validate.
     * @param userDetails The user details against which the token is to be validated.
     * @return true if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

}
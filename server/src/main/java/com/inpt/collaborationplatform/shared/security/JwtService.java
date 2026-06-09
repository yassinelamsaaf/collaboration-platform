package com.inpt.collaborationplatform.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inpt.collaborationplatform.Identity.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
@Data
public class JwtService {

    // Injected from application.yml
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey signingKey;

    @PostConstruct
    void validateJwtSecret() {
        signingKey = buildSigningKey();
    }

    // Build the signing key from the Base64-encoded secret string.
    private SecretKey getSigningKey() {
        return signingKey;
    }

    private SecretKey buildSigningKey() {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        } catch (DecodingException e) {
            throw new IllegalStateException("APP_JWT_SECRET must be a valid Base64-encoded HMAC key.", e);
        } catch (WeakKeyException e) {
            throw new IllegalStateException("APP_JWT_SECRET must decode to at least 32 bytes for HS256.", e);
        }
    }

    // Generate a short-lived access token
    // jti = JWT ID — unique per token, used for blacklisting
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())   // jti claim
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Generate a long-lived refresh token
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Parse and return all claims from a token
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Returns how many milliseconds until this token expires
    // Used to set the Redis TTL for blacklisted tokens
    public long getRemainingTtl(String token) {
        long expiry = extractExpiration(token).getTime();
        long now = System.currentTimeMillis();
        return Math.max(0, expiry - now);
    }
}

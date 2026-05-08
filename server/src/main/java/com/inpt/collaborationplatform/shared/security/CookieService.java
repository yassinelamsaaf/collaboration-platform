package com.inpt.collaborationplatform.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // Creates an HttpOnly cookie containing the access token
    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("access_token", token)
                .httpOnly(true)       // JS cannot read this cookie
                .secure(false)         // set to true to send over HTTPS
                .sameSite("Lax")      // Protects against CSRF
                .path("/")            // Available for all paths
                .maxAge(accessTokenExpiration / 1000) // Convert ms to seconds
                .build();
    }

    // Creates an HttpOnly cookie containing the refresh token
    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                // Restrict refresh token to only the refresh endpoint
                .path("/api/auth/refresh")
                .maxAge(refreshTokenExpiration / 1000)
                .build();
    }

    // Clears both cookies on logout by setting maxAge to 0
    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from("access_token", "")
                .httpOnly(true).secure(true).sameSite("Lax")
                .path("/").maxAge(0).build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(true).sameSite("Lax")
                .path("/api/auth/refresh").maxAge(0).build();
    }
}

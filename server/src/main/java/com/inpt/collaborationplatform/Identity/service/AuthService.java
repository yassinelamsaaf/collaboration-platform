package com.inpt.collaborationplatform.Identity.service;

import com.inpt.collaborationplatform.Identity.dto.request.LoginRequest;
import com.inpt.collaborationplatform.Identity.dto.request.RegisterRequest;
import com.inpt.collaborationplatform.Identity.dto.request.VerifyCodeRequest;
import com.inpt.collaborationplatform.Identity.dto.response.AuthResponse;
import com.inpt.collaborationplatform.Identity.entity.User;
import com.inpt.collaborationplatform.Identity.repository.UserRepository;
import com.inpt.collaborationplatform.shared.dto.MessageResponse;
import com.inpt.collaborationplatform.shared.exception.AccountNotVerifiedException;
import com.inpt.collaborationplatform.shared.exception.EmailAlreadyExistsException;
import com.inpt.collaborationplatform.shared.exception.InvalidTokenException;
import com.inpt.collaborationplatform.shared.security.CookieService;
import com.inpt.collaborationplatform.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final CookieService cookieService;

    @Value("${app.email-verification-expiry-hours}")
    private int verificationExpiryHours;

    // ─── REGISTER ────────────────────────────────────────────────────────────────

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new EmailAlreadyExistsException("Username already taken");
        }

        // Generate a cryptographically random 6-digit code (000000–999999)
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .verificationCode(code)
                // Code expires in 10 minutes
                .verificationCodeExpiry(LocalDateTime.now().plusMinutes(10))
                .enabled(false)
                .build();

        userRepository.save(user);
        emailService.sendVerificationCode(user.getEmail(), code);

        return new MessageResponse("Registration successful. A 6-digit code has been sent to your email.");
    }

    // ─── VERIFY CODE ──────────────────────────────────────────────────────────────

    @Transactional
    public MessageResponse verifyCode(VerifyCodeRequest request) {
        // Look up by email — user submits both email + code together
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidTokenException("No account found with this email"));

        // Already verified — no need to check the code
        if (user.isEnabled()) {
            return new MessageResponse("Account is already verified. You can log in.");
        }

        // Code doesn't match
        if (!request.getCode().equals(user.getVerificationCode())) {
            throw new InvalidTokenException("Invalid verification code");
        }

        // Code expired
        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification code has expired. Please request a new one.");
        }

        // All good — activate the account and clear the code
        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        return new MessageResponse("Email verified successfully. You can now log in.");
    }

    // ─── RESEND CODE // VERIFY EMAIL ──────────────────────────────────────────────────────────────

    @Transactional
    public MessageResponse resendCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("No account found with this email"));

        if (user.isEnabled()) {
            return new MessageResponse("Account is already verified.");
        }

        String newCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        user.setVerificationCode(newCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendVerificationCode(email, newCode);
        return new MessageResponse("A new verification code has been sent to your email.");
    }


    // ─── LOGIN ────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check account is verified
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Please verify your email before logging in");
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Store hashed refresh token in Redis
        // We hash it so even if Redis is compromised, tokens can't be reused
        String hashedRefresh = hashToken(refreshToken);
        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                hashedRefresh,
                jwtService.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        // Set both tokens as HttpOnly cookies in the response
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createRefreshTokenCookie(refreshToken).toString());

        return new AuthResponse(user.getId(), user.getEmail(), user.getRole().name(), user.getUsername());
    }

    // ─── REFRESH TOKEN ────────────────────────────────────────────────────────

    public MessageResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        // Extract refresh token from cookie
        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken == null) {
            throw new InvalidTokenException("Refresh token not found");
        }

        // Validate token structure and expiry
        Claims claims;
        try {
            claims = jwtService.extractAllClaims(refreshToken);
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String userId = claims.getSubject();

        // Check Redis for stored (hashed) refresh token
        String storedHash = redisTemplate.opsForValue().get("refresh:" + userId);
        if (storedHash == null || !storedHash.equals(hashToken(refreshToken))) {
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }

        // Load user and issue new access token
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(user);

        // Rotate refresh token — invalidate old, issue new
        // This limits the window of damage if a refresh token is stolen
        String newRefreshToken = jwtService.generateRefreshToken(user);
        String newHashedRefresh = hashToken(newRefreshToken);
        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                newHashedRefresh,
                jwtService.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        // Set new cookies
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createAccessTokenCookie(newAccessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createRefreshTokenCookie(newRefreshToken).toString());

        return new MessageResponse("Token refreshed successfully");
    }

    // ─── LOGOUT ───────────────────────────────────────────────────────────────

    public MessageResponse logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Blacklist current access token so it can't be reused before it expires
        String accessToken = extractCookie(request, "access_token");
        if (accessToken != null) {
            try {
                String jti = jwtService.extractJti(accessToken);
                long ttl = jwtService.getRemainingTtl(accessToken);
                if (ttl > 0) {
                    // Store the token's JTI in Redis until it would have expired naturally
                    redisTemplate.opsForValue().set(
                            "blacklist:" + jti,
                            "true",
                            ttl,
                            TimeUnit.MILLISECONDS
                    );
                }
            } catch (JwtException ignored) {
                // Token was already invalid — nothing to blacklist
            }
        }

        // 2. Delete refresh token from Redis
        String userId = getCurrentUserId();
        if (userId != null) {
            redisTemplate.delete("refresh:" + userId);
        }

        // 3. Clear both cookies from the browser
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.clearAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.clearRefreshTokenCookie().toString());

        return new MessageResponse("Logged out successfully");
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private String hashToken(String token) {
        // SHA-256 hash — one-way, so original token can't be recovered from Redis
        return DigestUtils.sha256Hex(token);
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }

    private String getCurrentUserId() {
        // Spring Security holds the authenticated user after the JWT filter runs
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user.getId();
        }
        return null;
    }
}

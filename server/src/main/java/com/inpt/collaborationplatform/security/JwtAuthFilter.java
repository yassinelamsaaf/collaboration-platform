package com.inpt.collaborationplatform.security;

import com.inpt.collaborationplatform.entity.User;
import com.inpt.collaborationplatform.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    // OncePerRequestFilter guarantees this runs exactly once per HTTP request

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extract access token from cookies
        String accessToken = extractTokenFromCookies(request, "access_token");

        // 2. If no token found, skip this filter (let Spring Security handle it)
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Validate token signature and expiry
            Claims claims = jwtService.extractAllClaims(accessToken);

            // 4. Check Redis blacklist — was this token explicitly invalidated?
            String jti = claims.getId();
            Boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + jti);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 5. Load user from database
            String userId = claims.getSubject();
            User user = userRepository.findById(userId).orElse(null);

            if (user != null && user.isEnabled()) {
                // 6. Build Spring Security authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Register authentication in the SecurityContext
                // Spring Security will now treat this request as authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (JwtException e) {
            // Token is invalid or expired — just continue without setting auth
            // The SecurityContext remains empty, so protected routes will return 401
        }

        filterChain.doFilter(request, response);
    }

    // Helper: find a cookie by name and return its value
    private String extractTokenFromCookies(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}

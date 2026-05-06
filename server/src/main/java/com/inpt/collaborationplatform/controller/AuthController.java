package com.inpt.collaborationplatform.controller;

import com.inpt.collaborationplatform.dto.auth.request.LoginRequest;
import com.inpt.collaborationplatform.dto.auth.request.RegisterRequest;
import com.inpt.collaborationplatform.dto.auth.request.VerifyCodeRequest;
import com.inpt.collaborationplatform.dto.auth.response.AuthResponse;
import com.inpt.collaborationplatform.dto.MessageResponse;
import com.inpt.collaborationplatform.entity.User;
import com.inpt.collaborationplatform.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    // User submits { "email": "...", "code": "123456" }
    @PostMapping("/verify-code")
    public ResponseEntity<MessageResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(authService.verifyCode(request));
    }

    // User submits { "email": "..." } to get a fresh code
    @PostMapping("/resend-code")
    public ResponseEntity<MessageResponse> resendCode(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendCode(email));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<MessageResponse> refresh(HttpServletRequest request,
                                                   HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request,
                                                  HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(request, response));
    }

    // Protected test endpoint — requires valid JWT cookie
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), user.getRole().name()));
    }
}
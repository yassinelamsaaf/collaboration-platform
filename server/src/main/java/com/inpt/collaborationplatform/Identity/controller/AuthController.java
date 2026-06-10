package com.inpt.collaborationplatform.Identity.controller;

import com.inpt.collaborationplatform.Identity.dto.request.ForgotPasswordRequest;
import com.inpt.collaborationplatform.Identity.dto.request.LoginRequest;
import com.inpt.collaborationplatform.Identity.dto.request.RegisterRequest;
import com.inpt.collaborationplatform.Identity.dto.request.ResetPasswordRequest;
import com.inpt.collaborationplatform.Identity.dto.request.VerifyCodeRequest;
import com.inpt.collaborationplatform.Identity.dto.response.AuthResponse;
import com.inpt.collaborationplatform.Identity.entity.User;
import com.inpt.collaborationplatform.Identity.service.AuthService;
import com.inpt.collaborationplatform.shared.dto.MessageResponse;

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

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/resend-reset-code")
    public ResponseEntity<MessageResponse> resendResetCode(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendResetCode(email));
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
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), user.getRole().name(), user.getUsername()));
    }
}
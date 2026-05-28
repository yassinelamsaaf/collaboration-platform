package com.inpt.collaborationplatform.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

// We don't return tokens in the body — they go in cookies.
// This just confirms successful login.
@Data
@AllArgsConstructor
public class AuthResponse {
    private String id;
    private String email;
    private String role;
    private String username;
}

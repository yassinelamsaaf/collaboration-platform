package com.inpt.collaborationplatform.shared.util;

import com.inpt.collaborationplatform.Identity.entity.User;
import org.springframework.security.core.Authentication;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static String currentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}

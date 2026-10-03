package com.moneymate.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static UserDetailsImpl getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) auth.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        UserDetailsImpl details = getCurrentUserDetails();
        return details != null ? details.getId() : null;
    }

    public static String getCurrentUserEmail() {
        UserDetailsImpl details = getCurrentUserDetails();
        return details != null ? details.getEmail() : null;
    }

    public static boolean isCurrentUserAdmin() {
        UserDetailsImpl details = getCurrentUserDetails();
        if (details == null) return false;
        for (GrantedAuthority ga : details.getAuthorities()) {
            if ("ROLE_ADMIN".equalsIgnoreCase(ga.getAuthority())) {
                return true;
            }
        }
        return "ADMIN".equalsIgnoreCase(details.getRole());
    }
}

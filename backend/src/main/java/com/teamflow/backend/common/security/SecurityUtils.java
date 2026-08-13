package com.teamflow.backend.common.security;

import com.teamflow.backend.domain.model.UserAccount;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserAccount getCurrentUserAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new AccessDeniedException("Access denied");
        }
        if (auth.getPrincipal() instanceof UserAccount userAccount) {
            return userAccount;
        }
        throw new AccessDeniedException("Access denied");
    }

    public static UUID getCurrentUserId() {
        return getCurrentUserAccount().id();
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                if ("ROLE_ADMIN".equals(authority.getAuthority()) || "ADMIN".equals(authority.getAuthority())) {
                    return true;
                }
            }
            if (auth.getPrincipal() instanceof UserAccount userAccount) {
                return "ADMIN".equalsIgnoreCase(userAccount.role());
            }
        }
        return false;
    }

    public static void checkSelfOrAdmin(UUID targetUserId) {
        if (isAdmin()) {
            return;
        }
        if (!getCurrentUserId().equals(targetUserId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}

package com.ciaranchaney.mcpserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Security service to check OAuth2 scopes for tool access control.
 */
@Component
public class ToolSecurityService {

    private final Environment environment;

    public ToolSecurityService(Environment environment) {
        this.environment = environment;
    }

    /**
     * Check if dev mode is active (OAuth2 disabled).
     */
    private boolean isDevMode() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    /**
     * Check if the current user has the required scope.
     */
    public boolean hasScope(String requiredScope) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // ONLY bypass security checks in dev mode
        if (authentication.getPrincipal() instanceof String) {
            if (isDevMode()) {
                return true; // Allow in dev mode ONLY
            } else {
                // In production with anonymous user = DENY
                return false;
            }
        }

        // Get JWT token
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();

            // Get scopes from token
            Object scopeClaim = jwt.getClaim("scope");
            if (scopeClaim instanceof String) {
                String scopeString = (String) scopeClaim;
                return scopeString.contains(requiredScope);
            } else if (scopeClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> scopes = (List<String>) scopeClaim;
                return scopes.contains(requiredScope);
            }
        }

        return false;
    }

    /**
     * Check if the current user has any of the required scopes.
     */
    public boolean hasAnyScope(String... requiredScopes) {
        for (String scope : requiredScopes) {
            if (hasScope(scope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current user has all of the required scopes.
     */
    public boolean hasAllScopes(String... requiredScopes) {
        for (String scope : requiredScopes) {
            if (!hasScope(scope)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get an access denied error response.
     */
    public Map<String, Object> accessDeniedResponse(String requiredScope) {
        return Map.of(
                "error", "Access Denied",
                "message", "This operation requires scope: " + requiredScope,
                "requiredScope", requiredScope
        );
    }

    /**
     * Get an access denied error response for multiple scopes.
     */
    public Map<String, Object> accessDeniedResponse(String... requiredScopes) {
        return Map.of(
                "error", "Access Denied",
                "message", "This operation requires one of the following scopes: " + String.join(", ", requiredScopes),
                "requiredScopes", List.of(requiredScopes)
        );
    }
}

package com.goat.identity.infrastructure.security;

import com.goat.identity.adapters.security.JwtAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Helper para acceder fácilmente a la información del usuario autenticado
 * desde el SecurityContext.
 */
@Component
public class SecurityContextHelper {

    /**
     * Obtiene el userId del usuario autenticado.
     *
     * @return UUID del usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado
     */
    public UUID getCurrentUserId() {
        return getCurrentUserIdOptional()
                .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));
    }

    /**
     * Obtiene el userId del usuario autenticado como Optional.
     *
     * @return Optional con el UUID del usuario autenticado
     */
    public Optional<UUID> getCurrentUserIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object details = authentication.getDetails();
        if (details instanceof JwtAuthenticationDetails jwtDetails) {
            return Optional.of(jwtDetails.getUserId());
        }

        // Fallback: intentar obtener desde el principal
        Object principal = authentication.getPrincipal();
        if (principal instanceof String userIdString) {
            try {
                return Optional.of(UUID.fromString(userIdString));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /**
     * Obtiene el email del usuario autenticado.
     *
     * @return Email del usuario autenticado
     */
    public Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object details = authentication.getDetails();
        if (details instanceof JwtAuthenticationDetails jwtDetails) {
            return Optional.of(jwtDetails.getEmail());
        }

        return Optional.empty();
    }

    /**
     * Verifica si el usuario actual tiene un rol específico.
     *
     * @param role Rol a verificar (sin prefijo ROLE_)
     * @return true si el usuario tiene el rol
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleWithPrefix = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }

    /**
     * Obtiene todos los roles del usuario autenticado.
     *
     * @return Lista de roles
     */
    public java.util.List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.List.of();
        }

        Object details = authentication.getDetails();
        if (details instanceof JwtAuthenticationDetails jwtDetails) {
            return jwtDetails.getRoles();
        }

        return java.util.List.of();
    }
}

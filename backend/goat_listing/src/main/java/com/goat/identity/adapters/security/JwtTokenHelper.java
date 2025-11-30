package com.goat.identity.adapters.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Helper para parsear tokens JWT y extraer información del usuario.
 * Compartido entre módulos para mantener consistencia.
 */
@Component
public class JwtTokenHelper {
    private final SecretKey secretKey;

    public JwtTokenHelper(
            @Value("${jwt.secret:defaultSecretKeyThatShouldBeChangedInProductionEnvironment}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Valida y parsea un token JWT, retornando todos los claims.
     *
     * @param token Token JWT (sin el prefijo "Bearer ")
     * @return Claims del token
     * @throws IllegalArgumentException si el token es inválido
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new IllegalArgumentException("Token inválido: " + e.getMessage());
        }
    }

    /**
     * Extrae el userId del token JWT.
     *
     * @param token Token JWT (sin el prefijo "Bearer ")
     * @return UUID del usuario
     * @throws IllegalArgumentException si el token es inválido o no contiene userId
     */
    public UUID extractUserId(String token) {
        Claims claims = parseToken(token);
        String subject = claims.getSubject();
        if (subject == null) {
            throw new IllegalArgumentException("Token no contiene userId");
        }
        return UUID.fromString(subject);
    }

    /**
     * Extrae el email del token JWT.
     *
     * @param token Token JWT (sin el prefijo "Bearer ")
     * @return Email del usuario
     */
    public String extractEmail(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Extrae los roles del token JWT.
     *
     * @param token Token JWT (sin el prefijo "Bearer ")
     * @return Lista de roles
     */
    public List<String> extractRoles(String token) {
        Claims claims = parseToken(token);
        Object rolesObj = claims.get("roles");
        
        if (rolesObj == null) {
            return new ArrayList<>();
        }
        
        if (rolesObj instanceof List) {
            List<?> rolesList = (List<?>) rolesObj;
            return rolesList.stream()
                    .map(Object::toString)
                    .toList();
        }
        
        return new ArrayList<>();
    }

    /**
     * Extrae el userId del header Authorization.
     * Remueve el prefijo "Bearer " si está presente.
     *
     * @param authHeader Header Authorization completo (ej: "Bearer token...")
     * @return UUID del usuario
     */
    public UUID extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new IllegalArgumentException("Header Authorization requerido");
        }

        String token = authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;

        return extractUserId(token);
    }
}

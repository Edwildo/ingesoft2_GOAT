package com.goat.identity.infrastructure.config;

import com.goat.identity.domain.exceptions.EmailAlreadyExistsException;
import com.goat.identity.domain.exceptions.EmailNotConfirmedException;
import com.goat.identity.domain.exceptions.InvalidCredentialsException;
import com.goat.identity.domain.exceptions.UserInactiveException;
import com.goat.listing.domain.exceptions.InsufficientRoleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones del dominio.
 */
@RestControllerAdvice
public class ExceptionHandlerConfig {
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Credenciales inválidas");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleUserInactive(UserInactiveException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Usuario inactivo");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(EmailNotConfirmedException.class)
    public ResponseEntity<Map<String, Object>> handleEmailNotConfirmed(EmailNotConfirmedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Email no confirmado");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Email ya existe");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Error de autenticación");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> body = new HashMap<>();
        
        // Si es un error de autenticación, usar código 401
        if (ex.getMessage() != null && ex.getMessage().contains("autentic")) {
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "Error de autenticación");
        } else {
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("error", "Estado inválido");
        }
        
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf((Integer) body.get("status"))).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        
        // Si es un error de token, usar código 401
        if (ex.getMessage() != null && (ex.getMessage().contains("Token") || ex.getMessage().contains("Authorization"))) {
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "Token inválido");
        } else {
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("error", "Argumento inválido");
        }
        
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf((Integer) body.get("status"))).body(body);
    }

    @ExceptionHandler(InsufficientRoleException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientRole(InsufficientRoleException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Permisos insuficientes");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}


package com.goat.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración global de CORS.
 * Permite todas las solicitudes desde cualquier origen.
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir todos los orígenes
        configuration.setAllowedOrigins(Arrays.asList("*"));
        
        // Permitir todos los métodos HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Permitir todos los headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciales (si es necesario en el futuro)
        configuration.setAllowCredentials(false);
        
        // Headers expuestos al frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // Cache de preflight requests (1 hora)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}


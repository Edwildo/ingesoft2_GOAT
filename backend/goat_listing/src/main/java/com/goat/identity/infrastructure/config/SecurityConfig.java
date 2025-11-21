package com.goat.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración de seguridad de Spring Security.
 * Permite acceso público al endpoint de login y configura CORS.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register", 
                                        "/api/auth/confirm-email", "/api/auth/otp", "/api/auth/verify").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}


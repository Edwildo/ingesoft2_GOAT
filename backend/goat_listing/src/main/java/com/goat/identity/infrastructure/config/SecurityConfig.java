package com.goat.identity.infrastructure.config;

import com.goat.identity.adapters.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración de seguridad de Spring Security.
 * Permite acceso público al endpoint de login y configura CORS.
 * Integra el filtro JWT para validación automática de tokens.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            CorsConfigurationSource corsConfigurationSource,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register", 
                                        "/api/auth/confirm-email", "/api/auth/otp", "/api/auth/verify").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        .requestMatchers("/api/navigation/**").permitAll()
                        // Permitir GET /api/listings (con query params) y GET /api/listings/{id} públicamente
                        // Nota: Spring Security ignora query parameters al hacer matching
                        .requestMatchers("GET", "/api/listings").permitAll()
                        .requestMatchers("GET", "/api/listings/{id}").permitAll()
                        // Permitir GET /api/catalog/sneakers públicamente (búsqueda y obtención)
                        .requestMatchers("GET", "/api/catalog/sneakers").permitAll()
                        .requestMatchers("GET", "/api/catalog/sneakers/{sku}").permitAll()
                        // POST /api/catalog/sneakers requiere autenticación
                        // Carrito requiere autenticación en todos los endpoints
                        .requestMatchers("/api/cart/**").authenticated()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}


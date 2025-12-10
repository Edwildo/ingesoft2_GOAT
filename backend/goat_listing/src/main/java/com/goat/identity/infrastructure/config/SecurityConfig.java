package com.goat.identity.infrastructure.config;

import com.goat.identity.adapters.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.config.Customizer;

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
                // Habilita CORS con la configuración definida en corsConfigurationSource
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Desactiva la protección CSRF (no necesaria en APIs stateless con JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configura la gestión de sesiones como STATELESS (no se guarda estado en el servidor)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Añade el filtro de autenticación JWT antes del filtro de autenticación por usuario/contraseña
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configuración de cabeceras de seguridad
                .headers(headers -> headers
                        // Evita que el navegador intente adivinar el tipo de contenido (X-Content-Type-Options: nosniff)
                        .contentTypeOptions(Customizer.withDefaults())

                        // Bloquea la carga del sitio dentro de iframes (X-Frame-Options: DENY) → protege contra clickjacking
                        .frameOptions(f -> f.deny())

                        // No envía información del referrer en las peticiones (Referrer-Policy: no-referrer)
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                )

                // Reglas de autorización de endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register",
                                "/api/auth/confirm-email", "/api/auth/otp", "/api/auth/verify").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        .requestMatchers("/api/navigation/**").permitAll()
                        // GET públicos
                        .requestMatchers(HttpMethod.GET, "/api/listings").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/listings/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/catalog/sneakers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/catalog/sneakers/{sku}").permitAll()
                        // Reglas de rol
                        .requestMatchers(HttpMethod.POST, "/api/listings").hasRole("SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/catalog/sneakers").hasRole("ADMIN")
                        // Carrito requiere autenticación en todos los endpoints
                        .requestMatchers("/api/cart/**").authenticated()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}

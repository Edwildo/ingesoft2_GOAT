package com.goat.identity.adapters.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filtro JWT que valida tokens automáticamente en cada request.
 * Establece el SecurityContext con la autenticación del usuario.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenHelper jwtTokenHelper;

    public JwtAuthenticationFilter(JwtTokenHelper jwtTokenHelper) {
        this.jwtTokenHelper = jwtTokenHelper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());
            
            // Extraer información del token
            UUID userId = jwtTokenHelper.extractUserId(token);
            String email = jwtTokenHelper.extractEmail(token);
            List<String> roles = jwtTokenHelper.extractRoles(token);

            // Crear authorities a partir de los roles
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // Crear objeto de autenticación
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(), // principal (userId como string para compatibilidad)
                            null, // credentials (no necesario con JWT)
                            authorities
                    );

            // Establecer detalles adicionales
            authentication.setDetails(new JwtAuthenticationDetails(userId, email, roles));

            // Establecer en el SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("Usuario autenticado: {} con roles: {}", email, roles);

        } catch (IllegalArgumentException e) {
            logger.warn("Token JWT inválido: {}", e.getMessage());
            // No establecemos autenticación, el request continúa sin autenticación
            // Spring Security rechazará el request si requiere autenticación
        }

        filterChain.doFilter(request, response);
    }
}

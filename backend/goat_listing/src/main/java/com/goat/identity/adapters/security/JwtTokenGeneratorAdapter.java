package com.goat.identity.adapters.security;

import com.goat.identity.ports.TokenGeneratorPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador que implementa TokenGeneratorPort usando JWT (io.jsonwebtoken).
 */
@Component
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {
    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtTokenGeneratorAdapter(
            @Value("${jwt.secret:defaultSecretKeyThatShouldBeChangedInProductionEnvironment}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    @Override
    public String generateToken(UUID userId, String email, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
}


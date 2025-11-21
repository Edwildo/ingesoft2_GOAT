package com.goat.identity.adapters.security;

import com.goat.identity.ports.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador que implementa PasswordEncoderPort usando BCrypt.
 */
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {
    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordEncoderAdapter() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}


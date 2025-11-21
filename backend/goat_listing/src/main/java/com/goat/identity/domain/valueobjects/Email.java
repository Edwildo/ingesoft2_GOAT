package com.goat.identity.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa un email válido.
 * Encapsula la validación de formato de email.
 */
public final class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final String value;

    private Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        String trimmedValue = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }
        this.value = trimmedValue;
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}


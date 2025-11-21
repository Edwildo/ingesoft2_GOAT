package com.goat.identity.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object que representa un hash de contraseña.
 * Encapsula el hash seguro de la contraseña.
 */
public final class PasswordHash {
    private final String value;

    private PasswordHash(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El hash de contraseña no puede estar vacío");
        }
        this.value = value;
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordHash that = (PasswordHash) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "PasswordHash{value='***'}";
    }
}


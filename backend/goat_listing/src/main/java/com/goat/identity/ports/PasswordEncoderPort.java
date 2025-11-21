package com.goat.identity.ports;

/**
 * Puerto (interfaz) para el codificador de contraseñas.
 * Define operaciones de hash y verificación sin depender de implementación.
 */
public interface PasswordEncoderPort {
    /**
     * Verifica si una contraseña en texto plano coincide con un hash.
     *
     * @param rawPassword Contraseña en texto plano
     * @param encodedPassword Hash de contraseña
     * @return true si coinciden, false en caso contrario
     */
    boolean matches(String rawPassword, String encodedPassword);

    /**
     * Genera un hash a partir de una contraseña en texto plano.
     *
     * @param rawPassword Contraseña en texto plano
     * @return Hash de la contraseña
     */
    String encode(String rawPassword);
}


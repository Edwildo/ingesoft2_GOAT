package com.goat.identity.ports;

import com.goat.identity.domain.entities.User;
import com.goat.identity.domain.valueobjects.Email;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto (interfaz) para el repositorio de usuarios.
 * Define las operaciones de persistencia sin depender de implementación.
 */
public interface UserRepository {
    /**
     * Busca un usuario por su email.
     *
     * @param email Email del usuario
     * @return Usuario encontrado o Optional vacío
     */
    Optional<User> findByEmail(Email email);

    /**
     * Guarda un usuario.
     *
     * @param user Usuario a guardar
     * @return Usuario guardado
     */
    User save(User user);

    /**
     * Busca un usuario por su ID.
     *
     * @param id ID del usuario
     * @return Usuario encontrado o Optional vacío
     */
    Optional<User> findById(UUID id);
}


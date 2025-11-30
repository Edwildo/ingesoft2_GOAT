package com.goat.identity.ports;

import com.goat.identity.domain.entities.Role;

import java.util.Optional;

/**
 * Puerto (interfaz) para el repositorio de roles.
 * Define las operaciones de consulta de roles sin depender de implementación.
 */
public interface RoleRepository {
    /**
     * Busca un rol por su código único.
     *
     * @param code Código del rol (ej: "SELLER", "BUYER", "CLIENT")
     * @return Rol encontrado o Optional vacío
     */
    Optional<Role> findByCode(String code);
}


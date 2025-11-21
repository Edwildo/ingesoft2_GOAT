package com.goat.identity.domain.entities;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa un rol del sistema.
 */
public class Role {
    private UUID id;
    private String code;  // Código único del rol: SUPER_ADMIN, SELLER, etc.
    private String name;  // Nombre descriptivo: Super Administrador, Vendedor, etc.

    public Role() {
        // Constructor público para JPA y mappers
    }

    public Role(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) && Objects.equals(code, role.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "Role{id=" + id + ", code='" + code + "', name='" + name + "'}";
    }
}


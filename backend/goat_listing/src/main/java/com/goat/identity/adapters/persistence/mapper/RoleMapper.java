package com.goat.identity.adapters.persistence.mapper;

import com.goat.identity.adapters.persistence.entity.RoleEntity;
import com.goat.identity.domain.entities.Role;

/**
 * Mapper para convertir entre entidades de dominio y entidades JPA de Role.
 */
public class RoleMapper {
    /**
     * Convierte una entidad JPA a una entidad de dominio.
     */
    public static Role toDomain(RoleEntity entity) {
        if (entity == null) {
            return null;
        }

        Role role = new Role();
        role.setId(entity.getId());
        role.setCode(entity.getCode());
        role.setName(entity.getName());
        return role;
    }

    /**
     * Convierte una entidad de dominio a una entidad JPA.
     */
    public static RoleEntity toEntity(Role role) {
        if (role == null) {
            return null;
        }

        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId());
        entity.setCode(role.getCode());
        entity.setName(role.getName());
        return entity;
    }
}


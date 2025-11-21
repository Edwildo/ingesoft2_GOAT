package com.goat.identity.adapters.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Entidad JPA que mapea a la tabla identity.roles.
 */
@Entity
@Table(name = "roles", schema = "identity")
public class RoleEntity {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    public RoleEntity() {
        // Constructor público requerido por JPA y por el mapper
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


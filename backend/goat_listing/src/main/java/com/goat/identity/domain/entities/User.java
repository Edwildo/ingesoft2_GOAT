package com.goat.identity.domain.entities;

import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.domain.valueobjects.PasswordHash;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa un usuario del sistema.
 * Agregado raíz del contexto Identity.
 */
public class User {
    private UUID id;
    private Email email;
    private PasswordHash passwordHash;
    private Boolean emailConfirmed;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Role> roles;

    protected User() {
        // Para JPA
        this.roles = new ArrayList<>();
    }

    public User(Email email, PasswordHash passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.emailConfirmed = false;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.roles = new ArrayList<>();
    }

    /**
     * Verifica si el usuario puede autenticarse.
     * Regla de negocio: debe estar activo y tener email confirmado.
     */
    public boolean canAuthenticate() {
        return Boolean.TRUE.equals(isActive) && Boolean.TRUE.equals(emailConfirmed);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(PasswordHash passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(Boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Role> getRoles() {
        return new ArrayList<>(roles);
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }

    public void addRole(Role role) {
        if (role != null && !roles.contains(role)) {
            roles.add(role);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email=" + email + ", isActive=" + isActive + "}";
    }
}


package com.goat.identity.adapters.persistence.mapper;

import com.goat.identity.adapters.persistence.entity.RoleEntity;
import com.goat.identity.adapters.persistence.entity.UserEntity;
import com.goat.identity.domain.entities.Role;
import com.goat.identity.domain.entities.User;
import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.domain.valueobjects.PasswordHash;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio y entidades JPA.
 */
public class UserMapper {
    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        User user = new User(
                Email.of(entity.getEmail()),
                PasswordHash.of(entity.getPasswordHash())
        );
        user.setId(entity.getId());
        user.setEmailConfirmed(entity.getEmailConfirmed());
        user.setIsActive(entity.getIsActive());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getRoles() != null) {
            List<Role> roles = entity.getRoles().stream()
                    .map(roleEntity -> {
                        Role role = new Role();
                        role.setId(roleEntity.getId());
                        role.setCode(roleEntity.getCode());
                        role.setName(roleEntity.getName());
                        return role;
                    })
                    .collect(Collectors.toList());
            user.setRoles(roles);
        }

        return user;
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail().getValue());
        entity.setPasswordHash(domain.getPasswordHash().getValue());
        entity.setEmailConfirmed(domain.getEmailConfirmed());
        entity.setIsActive(domain.getIsActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        if (domain.getRoles() != null) {
            List<RoleEntity> roleEntities = domain.getRoles().stream()
                    .map(role -> {
                        RoleEntity roleEntity = new RoleEntity();
                        roleEntity.setId(role.getId());
                        roleEntity.setCode(role.getCode());
                        roleEntity.setName(role.getName());
                        return roleEntity;
                    })
                    .collect(Collectors.toList());
            entity.setRoles(roleEntities);
        }

        return entity;
    }
}


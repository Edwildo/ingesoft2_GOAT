package com.goat.identity.adapters.persistence;

import com.goat.identity.adapters.persistence.entity.RoleEntity;
import com.goat.identity.adapters.persistence.mapper.RoleMapper;
import com.goat.identity.adapters.persistence.repository.RoleJpaRepository;
import com.goat.identity.domain.entities.Role;
import com.goat.identity.ports.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador que implementa RoleRepository usando PostgreSQL y Spring Data JPA.
 */
@Component
public class PostgreSQLRoleRepository implements RoleRepository {
    private final RoleJpaRepository jpaRepository;

    public PostgreSQLRoleRepository(RoleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Role> findByCode(String code) {
        Optional<RoleEntity> entity = jpaRepository.findByCode(code);
        return entity.map(RoleMapper::toDomain);
    }
}


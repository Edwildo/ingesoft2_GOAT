package com.goat.identity.adapters.persistence.repository;

import com.goat.identity.adapters.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para RoleEntity.
 */
@Repository
public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByCode(String code);
    Optional<RoleEntity> findByName(String name);
}

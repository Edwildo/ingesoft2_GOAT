package com.goat.identity.adapters.persistence.repository;

import com.goat.identity.adapters.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para UserEntity.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}


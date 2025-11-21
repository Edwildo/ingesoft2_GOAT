package com.goat.identity.adapters.persistence;

import com.goat.identity.adapters.persistence.entity.UserEntity;
import com.goat.identity.adapters.persistence.mapper.UserMapper;
import com.goat.identity.adapters.persistence.repository.UserJpaRepository;
import com.goat.identity.domain.entities.User;
import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.ports.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que implementa UserRepository usando PostgreSQL y Spring Data JPA.
 */
@Component
public class PostgreSQLUserRepository implements UserRepository {
    private final UserJpaRepository jpaRepository;

    public PostgreSQLUserRepository(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        Optional<UserEntity> entity = jpaRepository.findByEmail(email.getValue());
        return entity.map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity savedEntity = jpaRepository.save(entity);
        return UserMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        Optional<UserEntity> entity = jpaRepository.findById(id);
        return entity.map(UserMapper::toDomain);
    }
}


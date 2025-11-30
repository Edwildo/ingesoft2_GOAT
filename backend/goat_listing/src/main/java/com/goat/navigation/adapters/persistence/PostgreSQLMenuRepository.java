package com.goat.navigation.adapters.persistence;

import com.goat.navigation.adapters.persistence.entity.MenuEntity;
import com.goat.navigation.adapters.persistence.mapper.MenuMapper;
import com.goat.navigation.adapters.persistence.repository.MenuJpaRepository;
import com.goat.navigation.domain.entities.Menu;
import com.goat.navigation.ports.MenuRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador que implementa MenuRepository usando JPA.
 */
@Component
public class PostgreSQLMenuRepository implements MenuRepository {
    private final MenuJpaRepository jpaRepository;

    public PostgreSQLMenuRepository(MenuJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Menu> findPublicMenus() {
        List<MenuEntity> entities = jpaRepository.findPublicMenus();
        return MenuMapper.toDomainList(entities);
    }

    @Override
    public List<Menu> findByRoleCode(String roleCode) {
        List<MenuEntity> entities = jpaRepository.findByRoleCode(roleCode);
        return MenuMapper.toDomainList(entities);
    }

    @Override
    public List<Menu> findPublicAndRoleMenus(List<String> roleCodes) {
        List<MenuEntity> entities = jpaRepository.findPublicAndRoleMenus(roleCodes);
        return MenuMapper.toDomainList(entities);
    }
}

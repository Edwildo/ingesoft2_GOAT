package com.goat.navigation.adapters.persistence.mapper;

import com.goat.navigation.adapters.persistence.entity.MenuEntity;
import com.goat.navigation.domain.entities.Menu;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio y entidades JPA de Menu.
 */
public class MenuMapper {
    /**
     * Convierte una entidad JPA a una entidad de dominio.
     */
    public static Menu toDomain(MenuEntity entity) {
        if (entity == null) {
            return null;
        }

        Menu menu = new Menu();
        menu.setId(entity.getId());
        menu.setName(entity.getName());
        menu.setRoute(entity.getRoute());
        menu.setIcon(entity.getIcon());
        menu.setMenuOrder(entity.getMenuOrder());
        menu.setIsPublic(entity.getIsPublic());
        menu.setParentId(entity.getParentId());
        
        return menu;
    }

    /**
     * Convierte una lista de entidades JPA a entidades de dominio.
     */
    public static List<Menu> toDomainList(List<MenuEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(MenuMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad de dominio a una entidad JPA.
     */
    public static MenuEntity toEntity(Menu menu) {
        if (menu == null) {
            return null;
        }

        MenuEntity entity = new MenuEntity();
        entity.setId(menu.getId());
        entity.setName(menu.getName());
        entity.setRoute(menu.getRoute());
        entity.setIcon(menu.getIcon());
        entity.setMenuOrder(menu.getMenuOrder());
        entity.setIsPublic(menu.getIsPublic());
        entity.setParentId(menu.getParentId());

        return entity;
    }
}

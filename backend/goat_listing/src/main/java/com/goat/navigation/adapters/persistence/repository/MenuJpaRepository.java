package com.goat.navigation.adapters.persistence.repository;

import com.goat.navigation.adapters.persistence.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad MenuEntity.
 */
@Repository
public interface MenuJpaRepository extends JpaRepository<MenuEntity, UUID> {
    /**
     * Busca todos los menús públicos.
     */
    @Query("SELECT m FROM MenuEntity m WHERE m.isPublic = true ORDER BY m.menuOrder ASC NULLS LAST")
    List<MenuEntity> findPublicMenus();

    /**
     * Busca menús asignados a un rol específico a través de roles_menus.
     * Usa query nativa porque requiere join entre schemas diferentes.
     */
    @Query(value = "SELECT DISTINCT m.* FROM navigation.menus m " +
           "INNER JOIN navigation.roles_menus rm ON m.id = rm.menu_id " +
           "INNER JOIN identity.roles r ON rm.role_id = r.id " +
           "WHERE r.code = :roleCode " +
           "ORDER BY m.menu_order ASC NULLS LAST", nativeQuery = true)
    List<MenuEntity> findByRoleCode(@Param("roleCode") String roleCode);

    /**
     * Busca menús públicos o asignados a los roles especificados.
     * Usa query nativa porque requiere join entre schemas diferentes.
     */
    @Query(value = "SELECT DISTINCT m.* FROM navigation.menus m " +
           "LEFT JOIN navigation.roles_menus rm ON m.id = rm.menu_id " +
           "LEFT JOIN identity.roles r ON rm.role_id = r.id " +
           "WHERE m.is_public = true OR r.code IN :roleCodes " +
           "ORDER BY m.menu_order ASC NULLS LAST", nativeQuery = true)
    List<MenuEntity> findPublicAndRoleMenus(@Param("roleCodes") List<String> roleCodes);
}

package com.goat.navigation.ports;

import com.goat.navigation.domain.entities.Menu;

import java.util.List;

/**
 * Puerto (interfaz) para el repositorio de menús.
 * Define las operaciones de persistencia sin depender de implementación.
 */
public interface MenuRepository {
    /**
     * Busca todos los menús públicos.
     *
     * @return Lista de menús públicos
     */
    List<Menu> findPublicMenus();

    /**
     * Busca menús asignados a un rol específico.
     *
     * @param roleCode Código del rol (ej: "SELLER", "BUYER")
     * @return Lista de menús asignados al rol
     */
    List<Menu> findByRoleCode(String roleCode);

    /**
     * Busca todos los menús públicos y los asignados a los roles especificados.
     *
     * @param roleCodes Lista de códigos de roles
     * @return Lista de menús combinados
     */
    List<Menu> findPublicAndRoleMenus(List<String> roleCodes);
}

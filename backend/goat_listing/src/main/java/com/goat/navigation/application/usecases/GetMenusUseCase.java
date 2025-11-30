package com.goat.navigation.application.usecases;

import com.goat.navigation.application.dto.GetMenusResponse;
import com.goat.navigation.application.dto.MenuResponse;
import com.goat.navigation.domain.entities.Menu;
import com.goat.navigation.ports.MenuRepository;

import java.util.*;

/**
 * Caso de uso para obtener menús según los roles del usuario.
 */
public class GetMenusUseCase {
    private final MenuRepository menuRepository;

    public GetMenusUseCase(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    /**
     * Obtiene menús públicos y los asignados a los roles especificados.
     *
     * @param roleCodes Lista de códigos de roles (puede ser null o vacía)
     * @return Respuesta con menús jerárquicos ordenados
     */
    public GetMenusResponse execute(List<String> roleCodes) {
        List<Menu> menus;

        if (roleCodes == null || roleCodes.isEmpty()) {
            menus = menuRepository.findPublicMenus();
        } else {
            menus = menuRepository.findPublicAndRoleMenus(roleCodes);
        }

        List<MenuResponse> menuResponses = buildMenuHierarchy(menus);

        return new GetMenusResponse(menuResponses);
    }

    /**
     * Construye la jerarquía de menús y los convierte a DTOs.
     */
    private List<MenuResponse> buildMenuHierarchy(List<Menu> menus) {
        menus.sort(Comparator.comparing(Menu::getMenuOrder, Comparator.nullsLast(Comparator.naturalOrder())));

        Map<UUID, MenuResponse> menuMap = new HashMap<>();
        List<MenuResponse> rootMenus = new ArrayList<>();

        for (Menu menu : menus) {
            MenuResponse menuResponse = convertToResponse(menu);
            menuMap.put(menu.getId(), menuResponse);
        }

        for (Menu menu : menus) {
            MenuResponse menuResponse = menuMap.get(menu.getId());

            if (menu.getParentId() == null) {
                rootMenus.add(menuResponse);
            } else {
                MenuResponse parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    parent.getChildren().add(menuResponse);
                }
            }
        }

        for (MenuResponse menuResponse : menuMap.values()) {
            menuResponse.getChildren().sort(
                Comparator.comparing(MenuResponse::getMenuOrder, 
                    Comparator.nullsLast(Comparator.naturalOrder()))
            );
        }

        return rootMenus;
    }

    /**
     * Convierte una entidad Menu a MenuResponse.
     */
    private MenuResponse convertToResponse(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .route(menu.getRoute())
                .icon(menu.getIcon())
                .menuOrder(menu.getMenuOrder())
                .isPublic(menu.getIsPublic())
                .children(new ArrayList<>())
                .build();
    }
}

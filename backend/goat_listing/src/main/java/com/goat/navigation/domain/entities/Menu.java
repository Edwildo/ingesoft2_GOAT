package com.goat.navigation.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de dominio que representa un menú del sistema.
 * Agregado raíz del contexto Navigation.
 * 
 * Separada de MenuEntity (JPA) para mantener la Arquitectura Hexagonal:
 * - Menu (dominio) = Lógica de negocio pura, sin dependencias de frameworks
 * - MenuEntity (JPA) = Mapeo a base de datos, detalles de persistencia
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Menu {
    @EqualsAndHashCode.Include
    private UUID id;
    @EqualsAndHashCode.Include
    private String name;
    private String route;
    private String icon;
    private Integer menuOrder;
    private Boolean isPublic;
    private UUID parentId;
    private List<Menu> children = new ArrayList<>();

    public Menu(String name, String route, String icon, Integer menuOrder, Boolean isPublic) {
        this.name = name;
        this.route = route;
        this.icon = icon;
        this.menuOrder = menuOrder;
        this.isPublic = isPublic;
        this.children = new ArrayList<>();
    }

    /**
     * Obtiene una copia defensiva de la lista de hijos para mantener encapsulación.
     */
    public List<Menu> getChildren() {
        return new ArrayList<>(children);
    }

    /**
     * Establece la lista de hijos, creando una copia defensiva.
     */
    public void setChildren(List<Menu> children) {
        this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
    }

    public void addChild(Menu child) {
        if (child != null && !children.contains(child)) {
            children.add(child);
        }
    }
}

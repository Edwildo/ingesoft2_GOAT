package com.goat.navigation.adapters.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad JPA que mapea a la tabla navigation.menus.
 * Separada del dominio (Menu) para mantener la Arquitectura Hexagonal.
 */
@Entity
@Table(name = "menus", schema = "navigation")
@Getter
@Setter
@NoArgsConstructor
public class MenuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String route;

    @Column(length = 50)
    private String icon;

    @Column(name = "menu_order")
    private Integer menuOrder;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "parent_id", columnDefinition = "UUID")
    private UUID parentId;
}

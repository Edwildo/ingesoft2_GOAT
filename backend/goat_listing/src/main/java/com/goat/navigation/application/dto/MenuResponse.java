package com.goat.navigation.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO para la respuesta de menús.
 * Necesita getters/setters para serialización JSON y construcción con Builder.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private UUID id;
    private String name;
    private String route;
    private String icon;
    private Integer menuOrder;
    private Boolean isPublic;
    @Builder.Default
    private List<MenuResponse> children = new ArrayList<>();
}

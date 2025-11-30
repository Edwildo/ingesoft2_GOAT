package com.goat.navigation.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la respuesta del endpoint de obtener menús.
 * Necesita getters para serialización JSON y setters para construcción.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetMenusResponse {
    private List<MenuResponse> menus = new ArrayList<>();
}

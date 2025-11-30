package com.goat.listing.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para la respuesta de búsqueda de sneakers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchSneakersResponse {
    private List<SneakerResponse> sneakers;
    private int total;
    private int page;
    private int size;
}


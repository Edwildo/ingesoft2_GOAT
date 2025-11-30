package com.goat.listing.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la respuesta de lista de listings con paginación.
 * Necesita getters para serialización JSON y setters para construcción.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListingListResponse {
    private List<ListingResponse> listings = new ArrayList<>();
    private long total;
    private int page;
    private int size;
}

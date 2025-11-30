package com.goat.listing.domain.enums;

/**
 * Enum que representa el estado de un listing.
 */
public enum ListingStatus {
    DRAFT("DRAFT", "Borrador"),
    PUBLISHED("PUBLISHED", "Publicado"),
    ARCHIVED("ARCHIVED", "Archivado");

    private final String code;
    private final String description;

    ListingStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Convierte un código string a ListingStatus.
     */
    public static ListingStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ListingStatus status : ListingStatus.values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Código de estado inválido: " + code);
    }
}

"""DTO para filtros de búsqueda de sneakers."""

from typing import Optional

from pydantic import BaseModel, Field


class SearchFilters(BaseModel):
    """DTO para filtros de búsqueda."""

    brand: Optional[str] = Field(
        None, description="Filtrar por marca (ID o nombre)", examples=["nike"]
    )
    category: Optional[str] = Field(
        None, description="Filtrar por categoría (ID o slug)", examples=["basketball"]
    )
    gender: Optional[str] = Field(
        None, description="Filtrar por género (MALE, FEMALE, UNISEX)", examples=["UNISEX"]
    )
    collection: Optional[str] = Field(
        None, description="Filtrar por colección (ID o slug)", examples=["jordan-collection"]
    )
    search: Optional[str] = Field(
        None,
        description="Texto de búsqueda (busca en modelo y descripción)",
        examples=["jordan"],
    )
    page: int = Field(
        default=1, ge=1, description="Número de página (empezando en 1)", examples=[1]
    )
    size: int = Field(
        default=20, ge=1, le=100, description="Tamaño de página (máximo 100)", examples=[20]
    )


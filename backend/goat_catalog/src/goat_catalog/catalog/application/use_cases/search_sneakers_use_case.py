"""Caso de uso para buscar sneakers con filtros."""

from typing import List

from ...domain.entities.sneaker import Sneaker
from ...domain.repositories.sneaker_repository import SneakerRepository
from ..dto.search_filters import SearchFilters
from ..dto.sneaker_response import MediaResponse, SneakerResponse


class SearchSneakersUseCase:
    """Caso de uso para buscar sneakers con filtros opcionales."""

    def __init__(self, sneaker_repository: SneakerRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            sneaker_repository: Repositorio para buscar sneakers
        """
        self._sneaker_repository = sneaker_repository

    async def execute(self, filters: SearchFilters) -> tuple[List[SneakerResponse], int]:
        """Ejecuta la búsqueda de sneakers con filtros.

        Args:
            filters: Filtros de búsqueda

        Returns:
            Tupla con (lista de sneakers, total de resultados)
        """
        # Buscar en repositorio
        sneakers, total = await self._sneaker_repository.search(
            brand=filters.brand,
            category=filters.category,
            gender=filters.gender,
            collection=filters.collection,
            search_text=filters.search,
            page=filters.page,
            size=filters.size,
        )

        # Convertir entidades a DTOs
        sneaker_responses = [self._to_response(sneaker) for sneaker in sneakers]

        return sneaker_responses, total

    def _to_response(self, sneaker: Sneaker) -> SneakerResponse:
        """Convierte una entidad Sneaker a SneakerResponse.

        Args:
            sneaker: Entidad de dominio

        Returns:
            DTO de respuesta
        """
        media = sneaker.media
        media_response = MediaResponse(
            cover_image=media.cover_image,
            gallery=media.gallery,
        )

        return SneakerResponse(
            sku=sneaker.sku.value,
            brand=sneaker.brand,
            model=sneaker.model,
            gender=sneaker.gender,
            description=sneaker.description,
            categories=sneaker.categories,
            collections=sneaker.collections,
            media=media_response,
        )


"""Caso de uso para obtener todas las categorías."""

from typing import List

from ...domain.repositories.category_repository import CategoryRepository
from ..dto.category_response import CategoryResponse


class GetAllCategoriesUseCase:
    """Caso de uso para obtener todas las categorías del catálogo."""

    def __init__(self, category_repository: CategoryRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            category_repository: Repositorio para buscar categorías
        """
        self._category_repository = category_repository

    async def execute(self) -> List[CategoryResponse]:
        """Ejecuta la obtención de todas las categorías.

        Returns:
            Lista de categorías
        """
        categories = await self._category_repository.find_all()

        return [self._to_response(category) for category in categories]

    def _to_response(self, category) -> CategoryResponse:
        """Convierte una entidad Category a CategoryResponse.

        Args:
            category: Entidad de dominio

        Returns:
            DTO de respuesta
        """
        return CategoryResponse(
            id=category.id,
            name=category.name,
            slug=category.slug,
            description=category.description,
        )


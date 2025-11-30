"""Caso de uso para obtener todas las marcas."""

from typing import List

from ...domain.repositories.brand_repository import BrandRepository
from ..dto.brand_response import BrandResponse


class GetAllBrandsUseCase:
    """Caso de uso para obtener todas las marcas del catálogo."""

    def __init__(self, brand_repository: BrandRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            brand_repository: Repositorio para buscar marcas
        """
        self._brand_repository = brand_repository

    async def execute(self) -> List[BrandResponse]:
        """Ejecuta la obtención de todas las marcas.

        Returns:
            Lista de marcas
        """
        brands = await self._brand_repository.find_all()

        return [self._to_response(brand) for brand in brands]

    def _to_response(self, brand) -> BrandResponse:
        """Convierte una entidad Brand a BrandResponse.

        Args:
            brand: Entidad de dominio

        Returns:
            DTO de respuesta
        """
        return BrandResponse(
            id=brand.id,
            name=brand.name,
            slug=brand.slug.value,
            logo_url=brand.logo_url,
        )


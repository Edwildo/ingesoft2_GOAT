"""Caso de uso para obtener un sneaker por SKU."""

from ...domain.entities.sneaker import Sneaker
from ...domain.exceptions.sneaker_not_found_exception import SneakerNotFoundException
from ...domain.repositories.sneaker_repository import SneakerRepository
from ...domain.value_objects.sku import SKU
from ..dto.sneaker_response import MediaResponse, SneakerResponse


class GetSneakerBySkuUseCase:
    """Caso de uso para obtener un sneaker por su SKU.
    
    Este caso de uso es crítico para la validación de SKUs desde el backend Java.
    """

    def __init__(self, sneaker_repository: SneakerRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            sneaker_repository: Repositorio para buscar sneakers
        """
        self._sneaker_repository = sneaker_repository

    async def execute(self, sku: str) -> SneakerResponse:
        """Ejecuta la búsqueda de un sneaker por SKU.

        Args:
            sku: SKU del sneaker a buscar

        Returns:
            SneakerResponse con los datos del sneaker

        Raises:
            SneakerNotFoundException: Si el sneaker no existe
            ValueError: Si el SKU no es válido
        """
        # Validar y crear SKU value object
        sku_vo = SKU(sku)

        # Buscar en repositorio
        sneaker = await self._sneaker_repository.find_by_sku(sku_vo)

        if sneaker is None:
            raise SneakerNotFoundException(sku)

        # Convertir entidad a DTO
        return self._to_response(sneaker)

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


"""Caso de uso para obtener todas las colecciones."""

from typing import List

from ...domain.repositories.collection_repository import CollectionRepository
from ..dto.collection_response import CollectionResponse


class GetAllCollectionsUseCase:
    """Caso de uso para obtener todas las colecciones del catálogo."""

    def __init__(self, collection_repository: CollectionRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            collection_repository: Repositorio para buscar colecciones
        """
        self._collection_repository = collection_repository

    async def execute(self) -> List[CollectionResponse]:
        """Ejecuta la obtención de todas las colecciones.

        Returns:
            Lista de colecciones
        """
        collections = await self._collection_repository.find_all()

        return [self._to_response(collection) for collection in collections]

    def _to_response(self, collection) -> CollectionResponse:
        """Convierte una entidad Collection a CollectionResponse.

        Args:
            collection: Entidad de dominio

        Returns:
            DTO de respuesta
        """
        return CollectionResponse(
            id=collection.id,
            name=collection.name,
            slug=collection.slug,
            description=collection.description,
            release_date=collection.release_date,
        )


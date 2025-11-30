"""Repositorio (puerto) para persistencia de Collections."""

from abc import ABC, abstractmethod
from typing import List, Optional

from ..entities.collection import Collection


class CollectionRepository(ABC):
    """Interfaz abstracta para persistencia de Collections."""

    @abstractmethod
    async def find_by_id(self, collection_id: str) -> Optional[Collection]:
        """Busca una colección por su ID.

        Args:
            collection_id: ID de la colección a buscar

        Returns:
            Collection encontrada o None si no existe
        """
        pass

    @abstractmethod
    async def find_all(self) -> List[Collection]:
        """Retorna todas las colecciones.

        Returns:
            Lista de todas las colecciones
        """
        pass

    @abstractmethod
    async def exists_by_id(self, collection_id: str) -> bool:
        """Verifica si existe una colección con el ID dado.

        Args:
            collection_id: ID a verificar

        Returns:
            True si existe, False en caso contrario
        """
        pass

    @abstractmethod
    async def save(self, collection: Collection) -> None:
        """Guarda una colección (crea o actualiza).

        Args:
            collection: Collection a guardar
        """
        pass


"""Repositorio (puerto) para persistencia de Sneakers."""

from abc import ABC, abstractmethod
from typing import List, Optional

from ..entities.sneaker import Sneaker
from ..value_objects.sku import SKU


class SneakerRepository(ABC):
    """Interfaz abstracta para persistencia de Sneakers."""

    @abstractmethod
    async def find_by_sku(self, sku: SKU) -> Optional[Sneaker]:
        """Busca un sneaker por su SKU.

        Args:
            sku: SKU del sneaker a buscar

        Returns:
            Sneaker encontrado o None si no existe
        """
        pass

    @abstractmethod
    async def exists_by_sku(self, sku: SKU) -> bool:
        """Verifica si existe un sneaker con el SKU dado.

        Args:
            sku: SKU a verificar

        Returns:
            True si existe, False en caso contrario
        """
        pass

    @abstractmethod
    async def search(
        self,
        brand: Optional[str] = None,
        category: Optional[str] = None,
        gender: Optional[str] = None,
        collection: Optional[str] = None,
        search_text: Optional[str] = None,
        page: int = 1,
        size: int = 20,
    ) -> tuple[List[Sneaker], int]:
        """Busca sneakers con filtros opcionales.

        Args:
            brand: Filtrar por marca
            category: Filtrar por categoría
            gender: Filtrar por género (MALE, FEMALE, UNISEX)
            collection: Filtrar por colección
            search_text: Texto de búsqueda para buscar en modelo/descripción
            page: Número de página (empezando en 1)
            size: Tamaño de página

        Returns:
            Tupla con (lista de sneakers, total de resultados)
        """
        pass

    @abstractmethod
    async def save(self, sneaker: Sneaker) -> None:
        """Guarda un sneaker (crea o actualiza).

        Args:
            sneaker: Sneaker a guardar

        Raises:
            DuplicateSkuException: Si el SKU ya existe al crear
        """
        pass

    @abstractmethod
    async def delete(self, sku: SKU) -> None:
        """Elimina un sneaker por su SKU.

        Args:
            sku: SKU del sneaker a eliminar
        """
        pass


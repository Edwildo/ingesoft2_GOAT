"""Repositorio (puerto) para persistencia de Brands."""

from abc import ABC, abstractmethod
from typing import List, Optional

from ..entities.brand import Brand


class BrandRepository(ABC):
    """Interfaz abstracta para persistencia de Brands."""

    @abstractmethod
    async def find_by_id(self, brand_id: str) -> Optional[Brand]:
        """Busca una marca por su ID.

        Args:
            brand_id: ID de la marca a buscar

        Returns:
            Brand encontrada o None si no existe
        """
        pass

    @abstractmethod
    async def find_all(self) -> List[Brand]:
        """Retorna todas las marcas.

        Returns:
            Lista de todas las marcas
        """
        pass

    @abstractmethod
    async def find_by_slug(self, slug: str) -> Optional[Brand]:
        """Busca una marca por su slug.

        Args:
            slug: Slug de la marca

        Returns:
            Brand encontrada o None si no existe
        """
        pass

    @abstractmethod
    async def save(self, brand: Brand) -> None:
        """Guarda una marca (crea o actualiza).

        Args:
            brand: Brand a guardar
        """
        pass


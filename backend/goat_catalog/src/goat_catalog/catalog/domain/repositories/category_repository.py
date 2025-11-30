"""Repositorio (puerto) para persistencia de Categories."""

from abc import ABC, abstractmethod
from typing import List, Optional

from ..entities.category import Category


class CategoryRepository(ABC):
    """Interfaz abstracta para persistencia de Categories."""

    @abstractmethod
    async def find_by_id(self, category_id: str) -> Optional[Category]:
        """Busca una categoría por su ID.

        Args:
            category_id: ID de la categoría a buscar

        Returns:
            Category encontrada o None si no existe
        """
        pass

    @abstractmethod
    async def find_all(self) -> List[Category]:
        """Retorna todas las categorías.

        Returns:
            Lista de todas las categorías
        """
        pass

    @abstractmethod
    async def find_by_slug(self, slug: str) -> Optional[Category]:
        """Busca una categoría por su slug.

        Args:
            slug: Slug de la categoría

        Returns:
            Category encontrada o None si no existe
        """
        pass

    @abstractmethod
    async def exists_by_id(self, category_id: str) -> bool:
        """Verifica si existe una categoría con el ID dado.

        Args:
            category_id: ID a verificar

        Returns:
            True si existe, False en caso contrario
        """
        pass

    @abstractmethod
    async def save(self, category: Category) -> None:
        """Guarda una categoría (crea o actualiza).

        Args:
            category: Category a guardar
        """
        pass


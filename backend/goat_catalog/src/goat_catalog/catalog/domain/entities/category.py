"""Entidad Category - Representa una categoría en el dominio."""

from typing import Optional


class Category:
    """Entidad que representa una categoría con sus reglas de negocio."""

    def __init__(
        self,
        id: str,
        name: str,
        slug: str,
        description: Optional[str] = None,
    ) -> None:
        """Inicializa una Category.

        Args:
            id: Identificador único de la categoría
            name: Nombre de la categoría
            slug: Slug de la categoría (URL-friendly)
            description: Descripción de la categoría
        """
        self._id = id
        self._name = self._validate_name(name)
        self._slug = self._validate_slug(slug)
        self._description = description

    @classmethod
    def _validate_name(cls, name: str) -> str:
        """Valida el nombre de la categoría."""
        if not isinstance(name, str):
            raise ValueError(f"Nombre debe ser un string: {type(name)}")

        name = name.strip()

        if not name:
            raise ValueError("El nombre de la categoría no puede estar vacío")

        if len(name) > 100:
            raise ValueError(
                f"El nombre de la categoría no puede exceder 100 caracteres: {name}"
            )

        return name

    @classmethod
    def _validate_slug(cls, slug: str) -> str:
        """Valida el slug de la categoría."""
        if not isinstance(slug, str):
            raise ValueError(f"Slug debe ser un string: {type(slug)}")

        slug = slug.lower().strip()

        if not slug:
            raise ValueError("El slug de la categoría no puede estar vacío")

        return slug

    @property
    def id(self) -> str:
        """Retorna el ID de la categoría."""
        return self._id

    @property
    def name(self) -> str:
        """Retorna el nombre de la categoría."""
        return self._name

    @property
    def slug(self) -> str:
        """Retorna el slug de la categoría."""
        return self._slug

    @property
    def description(self) -> Optional[str]:
        """Retorna la descripción de la categoría."""
        return self._description

    def __eq__(self, other: object) -> bool:
        """Compara dos categorías por igualdad."""
        if not isinstance(other, Category):
            return False
        return self._id == other._id

    def __hash__(self) -> int:
        """Retorna el hash de la categoría."""
        return hash(self._id)

    def __repr__(self) -> str:
        """Representación de la categoría."""
        return f"Category(id='{self._id}', name='{self._name}')"


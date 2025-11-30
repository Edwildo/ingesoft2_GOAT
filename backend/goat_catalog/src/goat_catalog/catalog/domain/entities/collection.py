"""Entidad Collection - Representa una colección en el dominio."""

from datetime import datetime
from typing import Optional


class Collection:
    """Entidad que representa una colección con sus reglas de negocio."""

    def __init__(
        self,
        id: str,
        name: str,
        slug: str,
        description: Optional[str] = None,
        release_date: Optional[datetime] = None,
    ) -> None:
        """Inicializa una Collection.

        Args:
            id: Identificador único de la colección
            name: Nombre de la colección
            slug: Slug de la colección (URL-friendly)
            description: Descripción de la colección
            release_date: Fecha de lanzamiento de la colección
        """
        self._id = id
        self._name = self._validate_name(name)
        self._slug = self._validate_slug(slug)
        self._description = description
        self._release_date = release_date

    @classmethod
    def _validate_name(cls, name: str) -> str:
        """Valida el nombre de la colección."""
        if not isinstance(name, str):
            raise ValueError(f"Nombre debe ser un string: {type(name)}")

        name = name.strip()

        if not name:
            raise ValueError("El nombre de la colección no puede estar vacío")

        if len(name) > 100:
            raise ValueError(
                f"El nombre de la colección no puede exceder 100 caracteres: {name}"
            )

        return name

    @classmethod
    def _validate_slug(cls, slug: str) -> str:
        """Valida el slug de la colección."""
        if not isinstance(slug, str):
            raise ValueError(f"Slug debe ser un string: {type(slug)}")

        slug = slug.lower().strip()

        if not slug:
            raise ValueError("El slug de la colección no puede estar vacío")

        return slug

    @property
    def id(self) -> str:
        """Retorna el ID de la colección."""
        return self._id

    @property
    def name(self) -> str:
        """Retorna el nombre de la colección."""
        return self._name

    @property
    def slug(self) -> str:
        """Retorna el slug de la colección."""
        return self._slug

    @property
    def description(self) -> Optional[str]:
        """Retorna la descripción de la colección."""
        return self._description

    @property
    def release_date(self) -> Optional[datetime]:
        """Retorna la fecha de lanzamiento."""
        return self._release_date

    def __eq__(self, other: object) -> bool:
        """Compara dos colecciones por igualdad."""
        if not isinstance(other, Collection):
            return False
        return self._id == other._id

    def __hash__(self) -> int:
        """Retorna el hash de la colección."""
        return hash(self._id)

    def __repr__(self) -> str:
        """Representación de la colección."""
        return f"Collection(id='{self._id}', name='{self._name}')"


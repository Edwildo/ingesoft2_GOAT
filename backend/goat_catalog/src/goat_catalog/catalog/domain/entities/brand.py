"""Entidad Brand - Representa una marca en el dominio."""

from typing import Optional

from ..value_objects.brand_slug import BrandSlug


class Brand:
    """Entidad que representa una marca con sus reglas de negocio."""

    def __init__(
        self,
        id: str,
        name: str,
        slug: BrandSlug,
        logo_url: Optional[str] = None,
    ) -> None:
        """Inicializa una Brand.

        Args:
            id: Identificador único de la marca
            name: Nombre de la marca
            slug: Slug de la marca (URL-friendly)
            logo_url: URL del logo de la marca
        """
        self._id = id
        self._name = self._validate_name(name)
        self._slug = slug
        self._logo_url = logo_url

    @classmethod
    def _validate_name(cls, name: str) -> str:
        """Valida el nombre de la marca."""
        if not isinstance(name, str):
            raise ValueError(f"Nombre debe ser un string: {type(name)}")

        name = name.strip()

        if not name:
            raise ValueError("El nombre de la marca no puede estar vacío")

        if len(name) > 100:
            raise ValueError(f"El nombre de la marca no puede exceder 100 caracteres: {name}")

        return name

    @property
    def id(self) -> str:
        """Retorna el ID de la marca."""
        return self._id

    @property
    def name(self) -> str:
        """Retorna el nombre de la marca."""
        return self._name

    @property
    def slug(self) -> BrandSlug:
        """Retorna el slug de la marca."""
        return self._slug

    @property
    def logo_url(self) -> Optional[str]:
        """Retorna la URL del logo."""
        return self._logo_url

    def __eq__(self, other: object) -> bool:
        """Compara dos marcas por igualdad."""
        if not isinstance(other, Brand):
            return False
        return self._id == other._id

    def __hash__(self) -> int:
        """Retorna el hash de la marca."""
        return hash(self._id)

    def __repr__(self) -> str:
        """Representación de la marca."""
        return f"Brand(id='{self._id}', name='{self._name}')"


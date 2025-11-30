"""Value Object BrandSlug - Representa un slug de marca válido."""

import re
from typing_extensions import Self


class BrandSlug:
    """Value Object inmutable que representa un slug de marca válido.
    
    Un slug es una versión URL-friendly de un nombre de marca.
    """

    # Patrón para slug: minúsculas, números, guiones, 2-50 caracteres
    SLUG_PATTERN = re.compile(r"^[a-z0-9-]{2,50}$")

    def __init__(self, value: str) -> None:
        """Inicializa un BrandSlug.

        Args:
            value: String con el slug

        Raises:
            ValueError: Si el slug no es válido
        """
        self._value = self._validate(value)

    @classmethod
    def _validate(cls, value: str) -> str:
        """Valida que el slug sea válido."""
        if not isinstance(value, str):
            raise ValueError(f"BrandSlug debe ser un string: {type(value)}")

        value = value.lower().strip()

        if not value:
            raise ValueError("BrandSlug no puede estar vacío")

        if len(value) < 2:
            raise ValueError(f"BrandSlug debe tener al menos 2 caracteres: {value}")

        if len(value) > 50:
            raise ValueError(f"BrandSlug no puede exceder 50 caracteres: {value}")

        if not cls.SLUG_PATTERN.match(value):
            raise ValueError(
                f"BrandSlug inválido. Solo se permiten letras minúsculas, "
                f"números y guiones: {value}"
            )

        return value

    @classmethod
    def from_name(cls, name: str) -> "BrandSlug":
        """Crea un slug a partir de un nombre de marca.
        
        Args:
            name: Nombre de la marca
            
        Returns:
            BrandSlug creado desde el nombre
        """
        if not isinstance(name, str):
            raise ValueError(f"El nombre debe ser un string: {type(name)}")

        # Convertir a minúsculas y reemplazar espacios por guiones
        slug = name.lower().strip()
        slug = re.sub(r"[^\w\s-]", "", slug)  # Remover caracteres especiales
        slug = re.sub(r"[-\s]+", "-", slug)  # Reemplazar espacios y guiones múltiples
        slug = slug.strip("-")  # Remover guiones al inicio y final

        return cls(slug)

    @property
    def value(self) -> str:
        """Retorna el valor del slug."""
        return self._value

    def __eq__(self, other: object) -> bool:
        """Compara dos slugs por igualdad."""
        if not isinstance(other, BrandSlug):
            return False
        return self._value == other._value

    def __hash__(self) -> int:
        """Retorna el hash del slug."""
        return hash(self._value)

    def __str__(self) -> str:
        """Retorna el slug como string."""
        return self._value

    def __repr__(self) -> str:
        """Representación del slug."""
        return f"BrandSlug('{self._value}')"


"""Value Object SKU - Representa un SKU (Stock Keeping Unit) válido."""

import re
from typing_extensions import Self


class SKU:
    """Value Object inmutable que representa un SKU válido.
    
    Un SKU debe ser único, alfanumérico y seguir un formato específico.
    """

    # Patrón para SKU: alfanumérico, guiones y guiones bajos, 3-50 caracteres
    SKU_PATTERN = re.compile(r"^[A-Z0-9_-]{3,50}$")

    def __init__(self, value: str) -> None:
        """Inicializa un SKU.

        Args:
            value: String con el SKU

        Raises:
            ValueError: Si el SKU no es válido
        """
        self._value = self._validate(value)

    @classmethod
    def _validate(cls, value: str) -> str:
        """Valida que el SKU sea válido."""
        if not isinstance(value, str):
            raise ValueError(f"SKU debe ser un string: {type(value)}")

        value = value.upper().strip()

        if not value:
            raise ValueError("SKU no puede estar vacío")

        if len(value) < 3:
            raise ValueError(f"SKU debe tener al menos 3 caracteres: {value}")

        if len(value) > 50:
            raise ValueError(f"SKU no puede exceder 50 caracteres: {value}")

        if not cls.SKU_PATTERN.match(value):
            raise ValueError(
                f"SKU inválido. Solo se permiten letras mayúsculas, "
                f"números, guiones y guiones bajos: {value}"
            )

        return value

    @property
    def value(self) -> str:
        """Retorna el valor del SKU."""
        return self._value

    def __eq__(self, other: object) -> bool:
        """Compara dos SKUs por igualdad."""
        if not isinstance(other, SKU):
            return False
        return self._value == other._value

    def __hash__(self) -> int:
        """Retorna el hash del SKU."""
        return hash(self._value)

    def __str__(self) -> str:
        """Retorna el SKU como string."""
        return self._value

    def __repr__(self) -> str:
        """Representación del SKU."""
        return f"SKU('{self._value}')"


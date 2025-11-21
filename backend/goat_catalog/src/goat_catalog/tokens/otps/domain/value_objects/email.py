"""Value Object Email - Representa un email válido."""

import re

from typing_extensions import Self


class Email:
    """Value Object inmutables que representa un email válido."""

    # Patrón regex básico para validar email
    EMAIL_PATTERN = re.compile(
        r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"
    )

    def __init__(self, value: str) -> None:
        """Inicializa un Email.

        Args:
            value: String con el email

        Raises:
            ValueError: Si el email no es válido
        """
        self._value = self._validate(value)

    @classmethod
    def _validate(cls, value: str) -> str:
        """Valida que el email sea válido."""
        if not isinstance(value, str):
            raise ValueError(f"Email debe ser un string: {type(value)}")

        value = value.lower().strip()

        if not value:
            raise ValueError("Email no puede estar vacío")

        if not cls.EMAIL_PATTERN.match(value):
            raise ValueError(f"Email inválido: {value}")

        return value

    @property
    def value(self) -> str:
        """Retorna el valor del email."""
        return self._value

    def __eq__(self, other: object) -> bool:
        """Compara dos emails por igualdad."""
        if not isinstance(other, Email):
            return False
        return self._value == other._value

    def __hash__(self) -> int:
        """Retorna el hash del email."""
        return hash(self._value)

    def __str__(self) -> str:
        """Retorna el email como string."""
        return self._value

    def __repr__(self) -> str:
        """Representación del email."""
        return f"Email('{self._value}')"


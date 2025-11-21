"""Value Object OTPHash - Representa un hash de OTP."""

from typing_extensions import Self


class OTPHash:
    """Value Object inmutable que representa un hash de OTP."""

    def __init__(self, value: str) -> None:
        """Inicializa un OTPHash.

        Args:
            value: String con el hash hexadecimal

        Raises:
            ValueError: Si el hash no es válido
        """
        self._value = self._validate(value)

    @staticmethod
    def _validate(value: str) -> str:
        """Valida que el hash sea un string hexadecimal válido."""
        if not isinstance(value, str):
            raise ValueError(f"Hash debe ser un string: {type(value)}")
        if not value:
            raise ValueError("Hash no puede estar vacío")
        try:
            int(value, 16)  # Verifica que sea hexadecimal
            return value.lower().strip()
        except ValueError as e:
            raise ValueError(f"Hash inválido (debe ser hexadecimal): {value}") from e

    @property
    def value(self) -> str:
        """Retorna el valor del hash."""
        return self._value

    def __eq__(self, other: object) -> bool:
        """Compara dos hashes por igualdad."""
        if not isinstance(other, OTPHash):
            return False
        return self._value == other._value

    def __hash__(self) -> int:
        """Retorna el hash del objeto."""
        return hash(self._value)

    def __str__(self) -> str:
        """Retorna el hash como string."""
        return self._value

    def __repr__(self) -> str:
        """Representación del hash."""
        return f"OTPHash('{self._value}')"


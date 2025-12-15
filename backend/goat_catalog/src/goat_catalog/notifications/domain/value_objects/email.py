"""Value object para Email."""

from dataclasses import dataclass
import re


@dataclass(frozen=True)
class Email:
    """Value object que representa un email válido."""

    value: str

    def __post_init__(self) -> None:
        """Valida que el email tenga un formato válido."""
        if not self.value or not isinstance(self.value, str):
            raise ValueError("El email no puede estar vacío")
        
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        if not re.match(email_pattern, self.value):
            raise ValueError(f"El email '{self.value}' no tiene un formato válido")

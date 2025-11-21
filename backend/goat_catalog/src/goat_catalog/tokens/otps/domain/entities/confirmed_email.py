"""Entidad ConfirmedEmail - Representa un email confirmado en el dominio."""

from datetime import datetime

from ..value_objects.email import Email


class ConfirmedEmail:
    """Entidad que representa un email confirmado con sus reglas de negocio."""

    def __init__(
        self,
        email: Email,
        confirmed_at: datetime,
    ) -> None:
        """Inicializa un ConfirmedEmail.

        Args:
            email: Email del usuario
            confirmed_at: Fecha de confirmación
        """
        self._email = email
        self._confirmed_at = confirmed_at

    @property
    def email(self) -> Email:
        """Retorna el email confirmado."""
        return self._email

    @property
    def confirmed_at(self) -> datetime:
        """Retorna la fecha de confirmación."""
        return self._confirmed_at

    @classmethod
    def create(cls, email: Email) -> "ConfirmedEmail":
        """Factory method para crear un nuevo ConfirmedEmail.

        Args:
            email: Email del usuario a confirmar

        Returns:
            Nueva instancia de ConfirmedEmail
        """
        now = datetime.utcnow()
        return cls(
            email=email,
            confirmed_at=now,
        )


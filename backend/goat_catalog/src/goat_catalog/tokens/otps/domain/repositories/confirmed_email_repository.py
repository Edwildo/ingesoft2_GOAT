"""Interfaz del repositorio de emails confirmados (Puerto)."""

from abc import ABC, abstractmethod
from typing import Optional

from ..entities.confirmed_email import ConfirmedEmail
from ..value_objects.email import Email


class ConfirmedEmailRepository(ABC):
    """Interfaz abstracta para el repositorio de emails confirmados."""

    @abstractmethod
    async def save(self, confirmed_email: ConfirmedEmail) -> None:
        """Guarda un email confirmado.

        Args:
            confirmed_email: Email confirmado a guardar
        """
        raise NotImplementedError

    @abstractmethod
    async def find_by_email(self, email: Email) -> Optional[ConfirmedEmail]:
        """Busca un email confirmado por email.

        Args:
            email: Email del usuario

        Returns:
            ConfirmedEmail si existe, None en caso contrario
        """
        raise NotImplementedError

    @abstractmethod
    async def is_email_confirmed(self, email: Email) -> bool:
        """Verifica si un email está confirmado.

        Args:
            email: Email del usuario

        Returns:
            True si el email está confirmado, False en caso contrario
        """
        raise NotImplementedError


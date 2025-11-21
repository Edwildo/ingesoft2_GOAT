"""Interfaz del repositorio de OTPs (Puerto)."""

from abc import ABC, abstractmethod
from typing import Optional

from ..entities.otp_token import OTPToken
from ..value_objects.email import Email
from ..value_objects.otp_purpose import OTPPurpose


class OTPRepository(ABC):
    """Interfaz abstracta para el repositorio de OTPs."""

    @abstractmethod
    async def save(self, otp_token: OTPToken) -> None:
        """Guarda un OTPToken.

        Args:
            otp_token: Token OTP a guardar
        """
        raise NotImplementedError

    @abstractmethod
    async def find_by_email_and_purpose(
        self, email: Email, purpose: OTPPurpose
    ) -> Optional[OTPToken]:
        """Busca un OTPToken por email y propósito.

        Args:
            email: Email del usuario
            purpose: Propósito del OTP

        Returns:
            OTPToken si existe, None en caso contrario
        """
        raise NotImplementedError

    @abstractmethod
    async def delete(self, otp_token: OTPToken) -> None:
        """Elimina un OTPToken.

        Args:
            otp_token: Token OTP a eliminar
        """
        raise NotImplementedError

    @abstractmethod
    async def delete_by_email_and_purpose(
        self, email: Email, purpose: OTPPurpose
    ) -> None:
        """Elimina un OTPToken por email y propósito.

        Args:
            email: Email del usuario
            purpose: Propósito del OTP
        """
        raise NotImplementedError


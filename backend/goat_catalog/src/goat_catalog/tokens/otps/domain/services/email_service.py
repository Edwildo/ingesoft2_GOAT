"""Interfaz del servicio de email (Puerto)."""

from abc import ABC, abstractmethod

from ..value_objects.email import Email
from ..value_objects.otp_purpose import OTPPurpose


class EmailService(ABC):
    """Interfaz abstracta para el servicio de envío de emails."""

    @abstractmethod
    async def send_otp_email(
        self,
        to_email: Email,
        otp_code: str,
        purpose: OTPPurpose,
    ) -> None:
        """Envía un email con el código OTP.

        Args:
            to_email: Email destino
            otp_code: Código OTP a enviar
            purpose: Propósito del OTP (REGISTER, LOGIN, etc.)

        Raises:
            Exception: Si hay un error al enviar el email
        """
        raise NotImplementedError


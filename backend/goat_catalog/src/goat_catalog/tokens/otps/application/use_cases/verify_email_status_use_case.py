"""Caso de uso para verificar el estado de confirmación de un email."""

from ..dto.verify_email_status_response import VerifyEmailStatusResponse
from ...domain.repositories.confirmed_email_repository import ConfirmedEmailRepository
from ...domain.value_objects.email import Email

class VerifyEmailStatusUseCase:
    """Caso de uso para verificar el estado de confirmación de un email."""

    def __init__(self, confirmed_email_repository: ConfirmedEmailRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            confirmed_email_repository: Repositorio para emails confirmados
        """
        self._confirmed_email_repository = confirmed_email_repository

    async def execute(self, email_str: str) -> VerifyEmailStatusResponse:
        """Ejecuta la verificación del estado de confirmación de un email.

        Args:
            email_str: Email del usuario

        Returns:
            Response con el estado de confirmación del email
        """
        email = Email(email_str)
        is_confirmed = await self._confirmed_email_repository.is_email_confirmed(email)

        return VerifyEmailStatusResponse(
            confirmed=is_confirmed,
            email=email.value,
        )

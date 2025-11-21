"""Caso de uso para confirmar un email (verificar estado)."""

from ..dto.confirm_email_response import ConfirmEmailResponse
from ...domain.repositories.confirmed_email_repository import ConfirmedEmailRepository
from ...domain.value_objects.email import Email

class ConfirmEmailUseCase:
    """Caso de uso para verificar si un email está confirmado."""

    def __init__(self, confirmed_email_repository: ConfirmedEmailRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            confirmed_email_repository: Repositorio para emails confirmados
        """
        self._confirmed_email_repository = confirmed_email_repository

    async def execute(self, email_str: str) -> ConfirmEmailResponse:
        """Ejecuta la verificación de confirmación de un email.

        Args:
            email_str: Email del usuario

        Returns:
            Response con el estado de confirmación del email
        """
        email = Email(email_str)
        is_confirmed = await self._confirmed_email_repository.is_email_confirmed(email)

        if is_confirmed:
            return ConfirmEmailResponse(
                email=email.value,
                confirmed=True,
                message="Email confirmado exitosamente",
            )

        return ConfirmEmailResponse(
            email=email.value,
            confirmed=False,
            message="El email aún no ha sido confirmado (OTP no verificado)",
        )

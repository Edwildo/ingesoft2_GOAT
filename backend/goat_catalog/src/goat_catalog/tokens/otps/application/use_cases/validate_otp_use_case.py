"""Caso de uso para validar un OTP."""

import logging
import sys
from pathlib import Path

from ..dto.validate_otp_request import ValidateOTPRequest
from ..dto.validate_otp_response import ValidateOTPResponse
from ...domain.entities.confirmed_email import ConfirmedEmail
from ...domain.exceptions.otp_expired_exception import OTPExpiredException
from ...domain.exceptions.otp_invalid_exception import OTPInvalidException
from ...domain.exceptions.otp_max_attempts_exception import OTPMaxAttemptsException
from ...domain.repositories.confirmed_email_repository import ConfirmedEmailRepository
from ...domain.repositories.otp_repository import OTPRepository
from ...domain.value_objects.email import Email
from ...domain.value_objects.otp_purpose import OTPPurpose

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.config import get_settings
from goat_catalog.shared.utils.security import verify_otp_hash

logger = logging.getLogger(__name__)


class ValidateOTPUseCase:
    """Caso de uso para validar un OTP."""

    def __init__(
        self,
        otp_repository: OTPRepository,
        confirmed_email_repository: ConfirmedEmailRepository | None = None,
    ) -> None:
        """Inicializa el caso de uso.

        Args:
            otp_repository: Repositorio para buscar OTPs
            confirmed_email_repository: Repositorio para emails confirmados (opcional)
        """
        self._otp_repository = otp_repository
        self._confirmed_email_repository = confirmed_email_repository

    async def execute(self, request: ValidateOTPRequest) -> ValidateOTPResponse:
        """Ejecuta la validación de un OTP.

        Args:
            request: DTO con email, OTP y propósito

        Returns:
            Response con el resultado de la validación

        Raises:
            OTPExpiredException: Si el OTP ha expirado
            OTPInvalidException: Si el OTP es inválido
            OTPMaxAttemptsException: Si se alcanzó el máximo de intentos
        """
        settings = get_settings()
        email = Email(request.email)
        purpose = request.purpose

        logger.info(
            f"Validando OTP para email: {email.value}, "
            f"purpose: {purpose.value}"
        )

        # Buscar OTP en repositorio
        otp_token = await self._otp_repository.find_by_email_and_purpose(email, purpose)

        if not otp_token:
            logger.warning(
                f"OTP no encontrado para email: {email.value}, "
                f"purpose: {purpose.value}"
            )
            raise OTPInvalidException("OTP no encontrado")

        if otp_token.has_reached_max_attempts(settings.otp_max_attempts):
            logger.warning(
                f"Maximo de intentos alcanzado para email: {email.value}"
            )
            await self._otp_repository.delete(otp_token)
            raise OTPMaxAttemptsException(
                f"Maximo de intentos ({settings.otp_max_attempts}) alcanzado"
            )

        otp_token.increment_attempts()
        await self._otp_repository.save(otp_token)

        if otp_token.is_expired():
            logger.warning(
                f"OTP expirado para email: {email.value}. "
                f"Expiracion: {otp_token.expire_at}"
            )
            await self._otp_repository.delete(otp_token)
            raise OTPExpiredException("OTP ha expirado")

        # Verificar hash del OTP
        is_valid = verify_otp_hash(request.otp, otp_token.otp_hash.value)

        if not is_valid:
            logger.warning(
                f"OTP invalido para email: {email.value}"
            )
            await self._otp_repository.save(otp_token)
            raise OTPInvalidException("OTP invalido")

        logger.info(
            f"OTP valido para email: {email.value}, purpose: {purpose.value}"
        )

        await self._otp_repository.delete(otp_token)

        if purpose == OTPPurpose.EMAIL_CONFIRMATION and self._confirmed_email_repository:
            confirmed_email = ConfirmedEmail.create(email)
            await self._confirmed_email_repository.save(confirmed_email)
            logger.info(f"Email {email.value} marcado como confirmado")

        return ValidateOTPResponse(
            success=True,
            message="OTP válido",
            valid=True,
        )


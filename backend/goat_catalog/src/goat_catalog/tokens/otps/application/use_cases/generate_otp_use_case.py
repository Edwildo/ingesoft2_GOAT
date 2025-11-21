"""Caso de uso para generar un nuevo OTP."""

import logging
import sys
from pathlib import Path

from ..dto.generate_otp_request import GenerateOTPRequest
from ..dto.generate_otp_response import GenerateOTPResponse
from ...domain.entities.otp_token import OTPToken
from ...domain.repositories.otp_repository import OTPRepository
from ...domain.services.email_service import EmailService
from ...domain.services.otp_generator_service import OTPGeneratorService
from ...domain.value_objects.email import Email
from ...domain.value_objects.otp_hash import OTPHash

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.config import get_settings
from goat_catalog.shared.utils.security import hash_otp

logger = logging.getLogger(__name__)


class GenerateOTPUseCase:
    """Caso de uso para generar y guardar un nuevo OTP."""

    def __init__(
        self,
        otp_repository: OTPRepository,
        otp_generator_service: OTPGeneratorService,
        email_service: EmailService | None = None,
    ) -> None:
        """Inicializa el caso de uso.

        Args:
            otp_repository: Repositorio para persistir OTPs
            otp_generator_service: Servicio para generar códigos OTP
            email_service: Servicio para enviar emails (opcional)
        """
        self._otp_repository = otp_repository
        self._otp_generator_service = otp_generator_service
        self._email_service = email_service

    async def _send_otp_email(
        self,
        email: Email,
        otp_code: str,
        purpose,
    ) -> None:
        """Envía el email con el OTP si el servicio está disponible."""
        if self._email_service:
            try:
                await self._email_service.send_otp_email(email, otp_code, purpose)
            except Exception as e:
                logger.error(
                    f"Error al enviar email OTP a {email.value}: {e}",
                    exc_info=True,
                )
                # No lanzamos la excepción para no bloquear la generación del OTP
                # El OTP ya está guardado, solo falló el envío del email
        else:
            logger.warning(
                "Servicio de email no configurado. OTP generado pero no enviado."
            )

    async def execute(self, request: GenerateOTPRequest) -> GenerateOTPResponse:
        """Ejecuta la generación de un nuevo OTP.

        Args:
            request: DTO con email y propósito

        Returns:
            Response con el resultado de la operación
        """
        settings = get_settings()
        email = Email(request.email)
        purpose = request.purpose

        # Eliminar OTP anterior si existe
        await self._otp_repository.delete_by_email_and_purpose(email, purpose)

        # Generar nuevo OTP
        otp_code = self._otp_generator_service.generate()
        otp_hash = OTPHash(hash_otp(otp_code))

        # Crear entidad de dominio
        otp_token = OTPToken.create(
            email=email,
            purpose=purpose,
            otp_hash=otp_hash,
            expiration_minutes=settings.otp_expiration_minutes,
        )

        # Guardar en repositorio
        await self._otp_repository.save(otp_token)

        # Enviar email con el OTP (no bloqueante si falla)
        await self._send_otp_email(email, otp_code, purpose)

        return GenerateOTPResponse(
            success=True,
            message="OTP generado correctamente",
            expires_in_minutes=settings.otp_expiration_minutes,
        )


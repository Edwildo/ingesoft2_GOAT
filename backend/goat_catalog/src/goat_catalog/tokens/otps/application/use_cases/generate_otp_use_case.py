"""Caso de uso para generar un nuevo OTP."""

from ..dto.generate_otp_request import GenerateOTPRequest
from ..dto.generate_otp_response import GenerateOTPResponse
from ...domain.entities.otp_token import OTPToken
from ...domain.repositories.otp_repository import OTPRepository
from ...domain.services.otp_generator_service import OTPGeneratorService
from ...domain.value_objects.email import Email
from ...domain.value_objects.otp_hash import OTPHash
import sys
from pathlib import Path

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.config import get_settings
from goat_catalog.shared.utils.security import hash_otp


class GenerateOTPUseCase:
    """Caso de uso para generar y guardar un nuevo OTP."""

    def __init__(
        self,
        otp_repository: OTPRepository,
        otp_generator_service: OTPGeneratorService,
    ) -> None:
        """Inicializa el caso de uso.

        Args:
            otp_repository: Repositorio para persistir OTPs
            otp_generator_service: Servicio para generar códigos OTP
        """
        self._otp_repository = otp_repository
        self._otp_generator_service = otp_generator_service

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

        # TODO: Enviar email con el OTP
        # Por ahora solo retornamos success

        return GenerateOTPResponse(
            success=True,
            message="OTP generado correctamente",
            expires_in_minutes=settings.otp_expiration_minutes,
        )


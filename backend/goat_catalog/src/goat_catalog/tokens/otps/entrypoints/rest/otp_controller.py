"""Controller REST para operaciones con OTPs."""

from fastapi import APIRouter, HTTPException, Query, status

from ...application.dto.confirm_email_response import ConfirmEmailResponse
from ...application.dto.generate_otp_request import GenerateOTPRequest
from ...application.dto.generate_otp_response import GenerateOTPResponse
from ...application.dto.validate_otp_request import ValidateOTPRequest
from ...application.dto.validate_otp_response import ValidateOTPResponse
from ...application.dto.verify_email_status_response import VerifyEmailStatusResponse
from ...application.use_cases.confirm_email_use_case import ConfirmEmailUseCase
from ...application.use_cases.generate_otp_use_case import GenerateOTPUseCase
from ...application.use_cases.validate_otp_use_case import ValidateOTPUseCase
from ...application.use_cases.verify_email_status_use_case import VerifyEmailStatusUseCase
from ...domain.exceptions.otp_expired_exception import OTPExpiredException
from ...domain.exceptions.otp_invalid_exception import OTPInvalidException
from ...domain.exceptions.otp_max_attempts_exception import OTPMaxAttemptsException
from ...domain.repositories.confirmed_email_repository import ConfirmedEmailRepository
from ...domain.repositories.otp_repository import OTPRepository
from ...domain.services.otp_generator_service import OTPGeneratorService
from ...infrastructure.persistence.mongo_confirmed_email_repository import (
    MongoConfirmedEmailRepository,
)
from ...infrastructure.persistence.mongo_otp_repository import MongoOTPRepository

router = APIRouter(prefix="/api/auth", tags=["Authentication"])


def _get_generate_otp_use_case() -> GenerateOTPUseCase:
    """Factory para obtener instancia de GenerateOTPUseCase."""
    otp_repository: OTPRepository = MongoOTPRepository()
    otp_generator_service = OTPGeneratorService()
    return GenerateOTPUseCase(otp_repository, otp_generator_service)


def _get_validate_otp_use_case() -> ValidateOTPUseCase:
    """Factory para obtener instancia de ValidateOTPUseCase."""
    otp_repository: OTPRepository = MongoOTPRepository()
    confirmed_email_repository: ConfirmedEmailRepository = (
        MongoConfirmedEmailRepository()
    )
    return ValidateOTPUseCase(otp_repository, confirmed_email_repository)


def _get_verify_email_status_use_case() -> VerifyEmailStatusUseCase:
    """Factory para obtener instancia de VerifyEmailStatusUseCase."""
    confirmed_email_repository: ConfirmedEmailRepository = (
        MongoConfirmedEmailRepository()
    )
    return VerifyEmailStatusUseCase(confirmed_email_repository)


def _get_confirm_email_use_case() -> ConfirmEmailUseCase:
    """Factory para obtener instancia de ConfirmEmailUseCase."""
    confirmed_email_repository: ConfirmedEmailRepository = (
        MongoConfirmedEmailRepository()
    )
    return ConfirmEmailUseCase(confirmed_email_repository)


@router.post("/otp", response_model=GenerateOTPResponse, status_code=status.HTTP_201_CREATED)
async def generate_otp(request: GenerateOTPRequest) -> GenerateOTPResponse:
    """Genera un nuevo código OTP y lo envía por email.

    Args:
        request: Request con email y propósito del OTP

    Returns:
        Response con el resultado de la operación

    Raises:
        HTTPException: Si hay un error al generar el OTP
    """
    try:
        use_case = _get_generate_otp_use_case()
        return await use_case.execute(request)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        ) from e
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al generar OTP",
        ) from e


@router.post("/verify", response_model=ValidateOTPResponse, status_code=status.HTTP_200_OK)
async def verify_otp(request: ValidateOTPRequest) -> ValidateOTPResponse:
    """Valida un código OTP.

    Args:
        request: Request con email, OTP y propósito

    Returns:
        Response con el resultado de la validación

    Raises:
        HTTPException: Si el OTP es inválido, expirado o se alcanzó el máximo de intentos
    """
    try:
        use_case = _get_validate_otp_use_case()
        return await use_case.execute(request)
    except OTPExpiredException as e:
        raise HTTPException(
            status_code=status.HTTP_410_GONE,
            detail=str(e),
        ) from e
    except OTPInvalidException as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        ) from e
    except OTPMaxAttemptsException as e:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail=str(e),
        ) from e
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al validar OTP",
        ) from e


@router.get(
    "/verify-email-status",
    response_model=VerifyEmailStatusResponse,
    status_code=status.HTTP_200_OK,
)
async def verify_email_status(
    email: str = Query(..., description="Email del usuario", examples=["user@example.com"])
) -> VerifyEmailStatusResponse:
    """Verifica el estado de confirmación de un email.

    Args:
        email: Email del usuario a verificar

    Returns:
        Response con el estado de confirmación del email

    Raises:
        HTTPException: Si hay un error al verificar el estado
    """
    try:
        use_case = _get_verify_email_status_use_case()
        return await use_case.execute(email)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        ) from e
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al verificar estado del email",
        ) from e


@router.get(
    "/confirm-email",
    response_model=ConfirmEmailResponse,
    status_code=status.HTTP_200_OK,
)
async def confirm_email(
    email: str = Query(..., description="Email del usuario", examples=["user@example.com"])
) -> ConfirmEmailResponse:
    """Verifica si un email está confirmado.

    Args:
        email: Email del usuario a verificar

    Returns:
        Response con el estado de confirmación del email y mensaje

    Raises:
        HTTPException: Si hay un error al verificar la confirmación
    """
    try:
        use_case = _get_confirm_email_use_case()
        return await use_case.execute(email)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        ) from e
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al verificar confirmación del email",
        ) from e


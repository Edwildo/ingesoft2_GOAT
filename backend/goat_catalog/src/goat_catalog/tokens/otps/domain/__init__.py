"""Domain Layer - Reglas de negocio puras del agregado OTPs."""

# Exportar componentes principales del dominio para facilitar imports
from .entities import ConfirmedEmail, OTPToken
from .value_objects import Email, OTPHash, OTPPurpose
from .repositories import ConfirmedEmailRepository, OTPRepository
from .services import OTPGeneratorService
from .exceptions import (
    OTPExpiredException,
    OTPInvalidException,
    OTPMaxAttemptsException,
)

__all__ = [
    # Entities
    "ConfirmedEmail",
    "OTPToken",
    # Value Objects
    "Email",
    "OTPHash",
    "OTPPurpose",
    # Repositories (Ports)
    "ConfirmedEmailRepository",
    "OTPRepository",
    # Services
    "OTPGeneratorService",
    # Exceptions
    "OTPExpiredException",
    "OTPInvalidException",
    "OTPMaxAttemptsException",
]


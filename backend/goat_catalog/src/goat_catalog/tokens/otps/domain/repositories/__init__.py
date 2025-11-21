"""Interfaces de repositorio (Puertos) del dominio OTP."""

from .confirmed_email_repository import ConfirmedEmailRepository
from .otp_repository import OTPRepository

__all__ = ["ConfirmedEmailRepository", "OTPRepository"]


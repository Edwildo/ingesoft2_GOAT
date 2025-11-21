"""Excepciones del dominio OTP."""

from .otp_expired_exception import OTPExpiredException
from .otp_invalid_exception import OTPInvalidException
from .otp_max_attempts_exception import OTPMaxAttemptsException

__all__ = [
    "OTPExpiredException",
    "OTPInvalidException",
    "OTPMaxAttemptsException",
]


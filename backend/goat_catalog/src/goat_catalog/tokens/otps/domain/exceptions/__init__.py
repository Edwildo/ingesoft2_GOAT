"""Excepciones del dominio OTP."""

from .email_send_exception import (
    EmailAuthenticationException,
    EmailConnectionException,
    EmailDeliveryException,
    EmailSendException,
)
from .otp_expired_exception import OTPExpiredException
from .otp_invalid_exception import OTPInvalidException
from .otp_max_attempts_exception import OTPMaxAttemptsException

__all__ = [
    "EmailAuthenticationException",
    "EmailConnectionException",
    "EmailDeliveryException",
    "EmailSendException",
    "OTPExpiredException",
    "OTPInvalidException",
    "OTPMaxAttemptsException",
]


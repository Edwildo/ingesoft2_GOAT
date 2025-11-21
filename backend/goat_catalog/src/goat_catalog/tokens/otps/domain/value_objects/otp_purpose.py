"""Value Object OTPPurpose - Representa el propósito de un OTP."""

from enum import Enum


class OTPPurpose(str, Enum):
    """Enum que representa los propósitos válidos de un OTP."""

    REGISTER = "REGISTER"
    LOGIN = "LOGIN"
    RESET_PASSWORD = "RESET_PASSWORD"
    EMAIL_CONFIRMATION = "EMAIL_CONFIRMATION"

    def __str__(self) -> str:
        """Retorna el propósito como string."""
        return self.value


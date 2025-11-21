"""Excepción lanzada cuando un OTP ha expirado."""


class OTPExpiredException(Exception):
    """Excepción lanzada cuando se intenta validar un OTP expirado."""

    def __init__(self, message: str = "OTP ha expirado") -> None:
        """Inicializa la excepción.

        Args:
            message: Mensaje de error
        """
        super().__init__(message)
        self.message = message


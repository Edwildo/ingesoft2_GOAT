"""Excepción lanzada cuando un OTP es inválido."""


class OTPInvalidException(Exception):
    """Excepción lanzada cuando se intenta validar un OTP inválido."""

    def __init__(self, message: str = "OTP inválido") -> None:
        """Inicializa la excepción.

        Args:
            message: Mensaje de error
        """
        super().__init__(message)
        self.message = message


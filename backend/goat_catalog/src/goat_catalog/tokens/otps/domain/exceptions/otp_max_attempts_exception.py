"""Excepción lanzada cuando se alcanza el máximo de intentos."""


class OTPMaxAttemptsException(Exception):
    """Excepción lanzada cuando se supera el máximo de intentos de validación."""

    def __init__(self, message: str = "Máximo de intentos alcanzado") -> None:
        """Inicializa la excepción.

        Args:
            message: Mensaje de error
        """
        super().__init__(message)
        self.message = message


"""Excepción de dominio para errores al enviar emails."""


class EmailSendException(Exception):
    """Excepción base para errores al enviar emails."""

    def __init__(self, message: str, user_message: str | None = None) -> None:
        """Inicializa la excepción.

        Args:
            message: Mensaje técnico para logging
            user_message: Mensaje amigable para el usuario (opcional)
        """
        super().__init__(message)
        self.user_message = user_message or "Error al enviar el email. Por favor intenta más tarde."


class EmailAuthenticationException(EmailSendException):
    """Excepción para errores de autenticación SMTP."""

    def __init__(self, message: str) -> None:
        """Inicializa la excepción de autenticación."""
        super().__init__(
            message=message,
            user_message="Error de configuración del servidor de email. Contacta al administrador.",
        )


class EmailConnectionException(EmailSendException):
    """Excepción para errores de conexión SMTP."""

    def __init__(self, message: str) -> None:
        """Inicializa la excepción de conexión."""
        super().__init__(
            message=message,
            user_message="No se pudo conectar al servidor de email. Por favor intenta más tarde.",
        )


class EmailDeliveryException(EmailSendException):
    """Excepción para errores de entrega de email."""

    def __init__(self, message: str, recipient: str | None = None) -> None:
        """Inicializa la excepción de entrega."""
        user_msg = "No se pudo entregar el email."
        if recipient:
            user_msg = f"No se pudo entregar el email a {recipient}."
        
        super().__init__(
            message=message,
            user_message=user_msg,
        )


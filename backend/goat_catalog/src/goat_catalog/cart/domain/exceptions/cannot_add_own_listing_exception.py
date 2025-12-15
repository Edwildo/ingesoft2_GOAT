"""Excepción cuando se intenta agregar un listing propio."""


class CannotAddOwnListingException(Exception):
    """Excepción lanzada cuando un seller intenta agregar su propio listing."""

    def __init__(self, message: str = "No puedes agregar tus propios listings al carrito") -> None:
        """Inicializa la excepción.

        Args:
            message: Mensaje de error
        """
        super().__init__(message)
        self.message = message


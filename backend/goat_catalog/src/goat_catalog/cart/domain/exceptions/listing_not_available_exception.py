"""Excepción cuando un listing no está disponible."""


class ListingNotAvailableException(Exception):
    """Excepción lanzada cuando un listing no está disponible para agregar al carrito."""

    def __init__(self, message: str = "El listing no está disponible") -> None:
        """Inicializa la excepción.

        Args:
            message: Mensaje de error
        """
        super().__init__(message)
        self.message = message


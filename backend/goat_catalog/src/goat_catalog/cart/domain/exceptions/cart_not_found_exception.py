"""Excepción cuando no se encuentra un carrito."""


class CartNotFoundException(Exception):
    """Excepción lanzada cuando un carrito no existe."""

    def __init__(self, message: str = "Carrito no encontrado") -> None:
        """Inicializa la excepción.

        Args:
            message: Mensaje de error
        """
        super().__init__(message)
        self.message = message


"""Excepción cuando un item ya está en el carrito."""


class ItemAlreadyInCartException(Exception):
    """Excepción lanzada cuando se intenta agregar un item que ya existe."""

    def __init__(self, message: str = "El item ya está en el carrito") -> None:
        """Inicializa la excepción.

        Args:
            message: Mensaje de error
        """
        super().__init__(message)
        self.message = message


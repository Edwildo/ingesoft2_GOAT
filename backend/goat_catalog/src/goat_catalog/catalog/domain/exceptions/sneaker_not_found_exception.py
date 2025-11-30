"""Excepción cuando un sneaker no es encontrado."""


class SneakerNotFoundException(Exception):
    """Excepción lanzada cuando un sneaker no existe en el catálogo."""

    def __init__(self, sku: str) -> None:
        """Inicializa la excepción.

        Args:
            sku: SKU del sneaker no encontrado
        """
        self.sku = sku
        self.message = f"Sneaker con SKU '{sku}' no encontrado en el catálogo"
        super().__init__(self.message)


"""Excepción cuando se intenta crear un sneaker con SKU duplicado."""


class DuplicateSkuException(Exception):
    """Excepción lanzada cuando se intenta crear un sneaker con un SKU que ya existe."""

    def __init__(self, sku: str) -> None:
        """Inicializa la excepción.

        Args:
            sku: SKU duplicado
        """
        self.sku = sku
        self.message = f"Ya existe un sneaker con SKU '{sku}' en el catálogo"
        super().__init__(self.message)


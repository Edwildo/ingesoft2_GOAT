"""Excepción cuando una marca no es encontrada."""


class BrandNotFoundException(Exception):
    """Excepción lanzada cuando una marca no existe en el catálogo."""

    def __init__(self, brand_id: str) -> None:
        """Inicializa la excepción.

        Args:
            brand_id: ID de la marca no encontrada
        """
        self.brand_id = brand_id
        self.message = f"Marca con ID '{brand_id}' no encontrada en el catálogo"
        super().__init__(self.message)


"""Entidad Sneaker - Representa un sneaker en el dominio."""

from typing import List, Optional

from ..value_objects.media import Media
from ..value_objects.sku import SKU


class Sneaker:
    """Entidad que representa un sneaker con sus reglas de negocio."""

    def __init__(
        self,
        sku: SKU,
        brand: str,
        model: str,
        gender: str,
        description: Optional[str] = None,
        categories: Optional[List[str]] = None,
        collections: Optional[List[str]] = None,
        media: Optional[Media] = None,
    ) -> None:
        """Inicializa un Sneaker.

        Args:
            sku: SKU único del sneaker
            brand: Nombre o ID de la marca
            model: Modelo del sneaker
            gender: Género (MALE, FEMALE, UNISEX)
            description: Descripción del sneaker
            categories: Lista de IDs de categorías
            collections: Lista de IDs de colecciones
            media: Información de medios (imágenes)
        """
        self._sku = sku
        self._brand = self._validate_brand(brand)
        self._model = self._validate_model(model)
        self._gender = self._validate_gender(gender)
        self._description = description
        self._categories = categories or []
        self._collections = collections or []
        self._media = media or Media()

    @classmethod
    def _validate_brand(cls, brand: str) -> str:
        """Valida la marca del sneaker."""
        if not isinstance(brand, str):
            raise ValueError(f"Marca debe ser un string: {type(brand)}")

        brand = brand.strip()

        if not brand:
            raise ValueError("La marca del sneaker no puede estar vacía")

        return brand

    @classmethod
    def _validate_model(cls, model: str) -> str:
        """Valida el modelo del sneaker."""
        if not isinstance(model, str):
            raise ValueError(f"Modelo debe ser un string: {type(model)}")

        model = model.strip()

        if not model:
            raise ValueError("El modelo del sneaker no puede estar vacío")

        if len(model) > 200:
            raise ValueError(f"El modelo no puede exceder 200 caracteres: {model}")

        return model

    @classmethod
    def _validate_gender(cls, gender: str) -> str:
        """Valida el género del sneaker."""
        if not isinstance(gender, str):
            raise ValueError(f"Género debe ser un string: {type(gender)}")

        gender = gender.upper().strip()

        valid_genders = ["MALE", "FEMALE", "UNISEX"]
        if gender not in valid_genders:
            raise ValueError(
                f"Género inválido. Debe ser uno de: {', '.join(valid_genders)}. "
                f"Recibido: {gender}"
            )

        return gender

    @property
    def sku(self) -> SKU:
        """Retorna el SKU del sneaker."""
        return self._sku

    @property
    def brand(self) -> str:
        """Retorna la marca del sneaker."""
        return self._brand

    @property
    def model(self) -> str:
        """Retorna el modelo del sneaker."""
        return self._model

    @property
    def gender(self) -> str:
        """Retorna el género del sneaker."""
        return self._gender

    @property
    def description(self) -> Optional[str]:
        """Retorna la descripción del sneaker."""
        return self._description

    @property
    def categories(self) -> List[str]:
        """Retorna la lista de IDs de categorías."""
        return self._categories.copy()

    @property
    def collections(self) -> List[str]:
        """Retorna la lista de IDs de colecciones."""
        return self._collections.copy()

    @property
    def media(self) -> Media:
        """Retorna la información de medios."""
        return self._media

    def has_categories(self) -> bool:
        """Verifica si tiene categorías asociadas."""
        return len(self._categories) > 0

    def has_collections(self) -> bool:
        """Verifica si tiene colecciones asociadas."""
        return len(self._collections) > 0

    def __eq__(self, other: object) -> bool:
        """Compara dos sneakers por igualdad (basado en SKU)."""
        if not isinstance(other, Sneaker):
            return False
        return self._sku == other._sku

    def __hash__(self) -> int:
        """Retorna el hash del sneaker (basado en SKU)."""
        return hash(self._sku)

    def __repr__(self) -> str:
        """Representación del sneaker."""
        return f"Sneaker(sku={self._sku}, brand='{self._brand}', model='{self._brand}')"


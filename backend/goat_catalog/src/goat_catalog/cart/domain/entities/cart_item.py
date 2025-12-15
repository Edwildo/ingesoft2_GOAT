"""Entidad CartItem - Representa un item en el carrito."""

from datetime import datetime
from decimal import Decimal
from typing import Optional
from uuid import UUID, uuid4


class CartItem:
    """Entidad que representa un item dentro del carrito."""

    def __init__(
        self,
        listing_id: UUID,
        sneaker_sku: str,
        size: str,
        price: Decimal,
        brand: str,
        color: str,
        condition: str,
        cover_image: Optional[str] = None,
        item_id: Optional[UUID] = None,
        added_at: Optional[datetime] = None,
    ) -> None:
        """Inicializa un CartItem.

        Args:
            listing_id: ID del listing que se agregó al carrito
            sneaker_sku: SKU del sneaker
            size: Talla del sneaker
            price: Precio (snapshot al momento de agregar)
            brand: Marca del sneaker
            color: Color del sneaker
            condition: Condición del sneaker
            cover_image: URL de la imagen de portada
            item_id: ID del item (se genera si no se proporciona)
            added_at: Fecha en que se agregó (se genera si no se proporciona)
        """
        self._id = item_id or uuid4()
        self._listing_id = listing_id
        self._sneaker_sku = self._validate_string(sneaker_sku, "sneaker_sku")
        self._size = self._validate_string(size, "size")
        self._price = self._validate_price(price)
        self._brand = self._validate_string(brand, "brand")
        self._color = self._validate_string(color, "color")
        self._condition = self._validate_string(condition, "condition")
        self._cover_image = cover_image
        self._added_at = added_at or datetime.utcnow()

    @staticmethod
    def _validate_string(value: str, field_name: str) -> str:
        """Valida que un string no esté vacío."""
        if not isinstance(value, str):
            raise ValueError(f"{field_name} debe ser un string")
        value = value.strip()
        if not value:
            raise ValueError(f"{field_name} no puede estar vacío")
        return value

    @staticmethod
    def _validate_price(price: Decimal) -> Decimal:
        """Valida que el precio sea positivo."""
        if not isinstance(price, Decimal):
            raise ValueError("price debe ser un Decimal")
        if price <= 0:
            raise ValueError("price debe ser mayor a cero")
        return price

    @property
    def id(self) -> UUID:
        """Retorna el ID del item."""
        return self._id

    @property
    def listing_id(self) -> UUID:
        """Retorna el ID del listing."""
        return self._listing_id

    @property
    def sneaker_sku(self) -> str:
        """Retorna el SKU del sneaker."""
        return self._sneaker_sku

    @property
    def size(self) -> str:
        """Retorna la talla."""
        return self._size

    @property
    def price(self) -> Decimal:
        """Retorna el precio."""
        return self._price

    @property
    def brand(self) -> str:
        """Retorna la marca."""
        return self._brand

    @property
    def color(self) -> str:
        """Retorna el color."""
        return self._color

    @property
    def condition(self) -> str:
        """Retorna la condición."""
        return self._condition

    @property
    def cover_image(self) -> Optional[str]:
        """Retorna la imagen de portada."""
        return self._cover_image

    @property
    def added_at(self) -> datetime:
        """Retorna la fecha en que se agregó."""
        return self._added_at

    def __eq__(self, other: object) -> bool:
        """Compara dos items por igualdad (basado en listing_id)."""
        if not isinstance(other, CartItem):
            return False
        return self._listing_id == other._listing_id

    def __hash__(self) -> int:
        """Retorna el hash del item (basado en listing_id)."""
        return hash(self._listing_id)

    def __repr__(self) -> str:
        """Representación del item."""
        return (
            f"CartItem(id={self._id}, listing_id={self._listing_id}, "
            f"sku={self._sneaker_sku}, price={self._price})"
        )


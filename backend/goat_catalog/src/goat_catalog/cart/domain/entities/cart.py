"""Entidad Cart - Representa un carrito de compras."""

from datetime import datetime, timedelta
from decimal import Decimal
from typing import List, Optional
from uuid import UUID, uuid4

from .cart_item import CartItem


class Cart:
    """Agregado raíz del bounded context Cart."""

    def __init__(
        self,
        user_id: UUID,
        items: Optional[List[CartItem]] = None,
        cart_id: Optional[UUID] = None,
        created_at: Optional[datetime] = None,
        updated_at: Optional[datetime] = None,
        expire_at: Optional[datetime] = None,
        expiration_days: int = 30,
    ) -> None:
        """Inicializa un Cart.

        Args:
            user_id: ID del usuario propietario del carrito
            items: Lista de items en el carrito
            cart_id: ID del carrito (se genera si no se proporciona)
            created_at: Fecha de creación (se genera si no se proporciona)
            updated_at: Fecha de última actualización
            expire_at: Fecha de expiración para TTL
            expiration_days: Días hasta la expiración (default: 30)
        """
        self._id = cart_id or uuid4()
        self._user_id = user_id
        self._items = items or []
        now = datetime.utcnow()
        self._created_at = created_at or now
        self._updated_at = updated_at or now
        # TTL: el carrito expira después de N días de inactividad
        self._expire_at = expire_at or (now + timedelta(days=expiration_days))

    @classmethod
    def create_new(cls, user_id: UUID, expiration_days: int = 30) -> "Cart":
        """Factory method para crear un nuevo carrito.

        Args:
            user_id: ID del usuario
            expiration_days: Días hasta la expiración

        Returns:
            Nueva instancia de Cart
        """
        return cls(user_id=user_id, expiration_days=expiration_days)

    def add_item(self, item: CartItem) -> None:
        """Agrega un item al carrito si no existe ya.

        Args:
            item: Item a agregar

        Raises:
            ValueError: Si el item ya existe en el carrito
        """
        if self.contains_item(item.listing_id):
            raise ValueError(
                f"El listing {item.listing_id} ya está en el carrito"
            )
        self._items.append(item)
        self._updated_at = datetime.utcnow()
        # Resetear expiración al actualizar
        self._expire_at = datetime.utcnow() + timedelta(days=30)

    def remove_item(self, item_id: UUID) -> None:
        """Remueve un item del carrito por su ID.

        Args:
            item_id: ID del item a remover
        """
        self._items = [item for item in self._items if item.id != item_id]
        self._updated_at = datetime.utcnow()
        # Resetear expiración al actualizar
        self._expire_at = datetime.utcnow() + timedelta(days=30)

    def contains_item(self, listing_id: UUID) -> bool:
        """Verifica si un listing ya está en el carrito.

        Args:
            listing_id: ID del listing a verificar

        Returns:
            True si el listing está en el carrito, False en caso contrario
        """
        return any(item.listing_id == listing_id for item in self._items)

    def calculate_total(self) -> Decimal:
        """Calcula el precio total del carrito.

        Returns:
            Suma de todos los precios de los items
        """
        return sum(item.price for item in self._items)

    def get_total_items(self) -> int:
        """Retorna la cantidad de items en el carrito.

        Returns:
            Número de items
        """
        return len(self._items)

    def clear(self) -> None:
        """Vacía el carrito."""
        self._items.clear()
        self._updated_at = datetime.utcnow()

    def is_empty(self) -> bool:
        """Verifica si el carrito está vacío.

        Returns:
            True si está vacío, False en caso contrario
        """
        return len(self._items) == 0

    @property
    def id(self) -> UUID:
        """Retorna el ID del carrito."""
        return self._id

    @property
    def user_id(self) -> UUID:
        """Retorna el ID del usuario."""
        return self._user_id

    @property
    def items(self) -> List[CartItem]:
        """Retorna la lista de items (copia)."""
        return self._items.copy()

    @property
    def created_at(self) -> datetime:
        """Retorna la fecha de creación."""
        return self._created_at

    @property
    def updated_at(self) -> datetime:
        """Retorna la fecha de última actualización."""
        return self._updated_at

    @property
    def expire_at(self) -> datetime:
        """Retorna la fecha de expiración (para TTL)."""
        return self._expire_at

    def __repr__(self) -> str:
        """Representación del carrito."""
        return (
            f"Cart(id={self._id}, user_id={self._user_id}, "
            f"items={len(self._items)}, total={self.calculate_total()})"
        )


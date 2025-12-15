"""Interfaz del repositorio de carritos."""

from typing import Optional
from uuid import UUID

from ..entities.cart import Cart


class CartRepository:
    """Puerto (interfaz) para persistencia de carritos."""

    async def find_active_by_user(self, user_id: UUID) -> Optional[Cart]:
        """Busca el carrito activo de un usuario.

        Args:
            user_id: ID del usuario

        Returns:
            Cart si existe, None en caso contrario
        """
        raise NotImplementedError

    async def save(self, cart: Cart) -> None:
        """Guarda o actualiza un carrito.

        Args:
            cart: Carrito a guardar
        """
        raise NotImplementedError

    async def delete(self, cart_id: UUID) -> None:
        """Elimina un carrito.

        Args:
            cart_id: ID del carrito a eliminar
        """
        raise NotImplementedError


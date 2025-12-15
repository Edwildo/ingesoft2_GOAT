"""Caso de uso para vaciar el carrito."""

import logging
from uuid import UUID

from ...domain.repositories import CartRepository

logger = logging.getLogger(__name__)


class ClearCartUseCase:
    """Caso de uso para vaciar el carrito de un usuario."""

    def __init__(self, cart_repository: CartRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            cart_repository: Repositorio para acceder a los carritos
        """
        self._cart_repository = cart_repository

    async def execute(self, user_id: UUID) -> None:
        """Vacía el carrito del usuario.

        Args:
            user_id: ID del usuario
        """
        logger.info("Vaciando carrito del usuario %s", user_id)

        # 1. Obtener carrito del usuario
        cart = await self._cart_repository.find_active_by_user(user_id)

        if cart is None or cart.is_empty():
            logger.info("El carrito del usuario %s ya estaba vacío", user_id)
            return

        # 2. Limpiar items
        cart.clear()

        # 3. Guardar cambios
        await self._cart_repository.save(cart)

        logger.info("Carrito del usuario %s vaciado", user_id)


"""Caso de uso para obtener el carrito de un usuario."""

import logging
from uuid import UUID

from ...domain.entities import Cart
from ...domain.repositories import CartRepository

logger = logging.getLogger(__name__)


class GetCartUseCase:
    """Caso de uso para obtener el carrito de un usuario."""

    def __init__(self, cart_repository: CartRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            cart_repository: Repositorio para acceder a los carritos
        """
        self._cart_repository = cart_repository

    async def execute(self, user_id: UUID) -> Cart:
        """Obtiene el carrito activo de un usuario o crea uno vacío.

        Args:
            user_id: ID del usuario

        Returns:
            Cart del usuario (activo o nuevo vacío)
        """
        logger.info("Obteniendo carrito del usuario %s", user_id)

        cart = await self._cart_repository.find_active_by_user(user_id)

        if cart is None:
            logger.debug("No se encontró carrito activo para usuario %s, creando uno nuevo", user_id)
            cart = Cart.create_new(user_id)

        return cart


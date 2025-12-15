"""Caso de uso para remover un item del carrito."""

import logging
from uuid import UUID

from ...domain.exceptions import CartNotFoundException
from ...domain.repositories import CartRepository

logger = logging.getLogger(__name__)


class RemoveItemFromCartUseCase:
    """Caso de uso para remover un item del carrito."""

    def __init__(self, cart_repository: CartRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            cart_repository: Repositorio para acceder a los carritos
        """
        self._cart_repository = cart_repository

    async def execute(self, user_id: UUID, item_id: UUID) -> None:
        """Remueve un item del carrito del usuario.

        Args:
            user_id: ID del usuario
            item_id: ID del item a remover

        Raises:
            CartNotFoundException: Si el carrito no existe
            ValueError: Si el item no está en el carrito
        """
        logger.info("Removiendo item %s del carrito del usuario %s", item_id, user_id)

        # 1. Obtener carrito del usuario
        cart = await self._cart_repository.find_active_by_user(user_id)
        if cart is None:
            logger.warning("Carrito no encontrado para usuario %s", user_id)
            raise CartNotFoundException("Carrito no encontrado")

        # 2. Verificar que el item existe
        item_exists = any(item.id == item_id for item in cart.items)
        if not item_exists:
            logger.warning("Item %s no encontrado en el carrito del usuario %s", item_id, user_id)
            raise ValueError(f"El item {item_id} no se encuentra en el carrito")

        # 3. Remover el item
        cart.remove_item(item_id)

        # 4. Guardar cambios
        await self._cart_repository.save(cart)

        logger.info("Item %s removido del carrito del usuario %s", item_id, user_id)


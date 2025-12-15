"""Caso de uso para agregar un item al carrito."""

import logging
from decimal import Decimal
from uuid import UUID

from ...domain.entities import Cart, CartItem
from ...domain.exceptions import (
    CannotAddOwnListingException,
    ItemAlreadyInCartException,
    ListingNotAvailableException,
)
from ...domain.repositories import CartRepository
from ..dto.add_item_request import AddItemRequest

logger = logging.getLogger(__name__)


class AddItemToCartUseCase:
    """Caso de uso para agregar un item al carrito."""

    def __init__(self, cart_repository: CartRepository) -> None:
        """Inicializa el caso de uso.

        Args:
            cart_repository: Repositorio para acceder a los carritos
        """
        self._cart_repository = cart_repository

    async def execute(self, user_id: UUID, request: AddItemRequest, seller_id: UUID | None = None) -> Cart:
        """Agrega un item al carrito del usuario.

        Args:
            user_id: ID del usuario que agrega el item
            request: Información del listing a agregar (ya validada por Java)
            seller_id: ID del seller del listing (opcional, para validar que no sea propio)

        Returns:
            Cart actualizado con el nuevo item

        Raises:
            ItemAlreadyInCartException: Si el listing ya está en el carrito
            CannotAddOwnListingException: Si el usuario intenta agregar su propio listing
            ListingNotAvailableException: Si el listing no está disponible
        """
        logger.info("Agregando listing %s al carrito del usuario %s", request.listingId, user_id)

        # 1. Validar que no sea el listing propio (si se proporciona seller_id)
        if seller_id and seller_id == user_id:
            logger.warning("Usuario %s intentó agregar su propio listing %s", user_id, request.listingId)
            raise CannotAddOwnListingException("No puedes agregar tus propios listings al carrito")

        # 2. Buscar o crear carrito activo
        cart = await self._cart_repository.find_active_by_user(user_id)
        if cart is None:
            logger.debug("Creando nuevo carrito para usuario %s", user_id)
            cart = Cart.create_new(user_id)

        # 3. Verificar que no esté duplicado
        listing_uuid = UUID(request.listingId)
        if cart.contains_item(listing_uuid):
            logger.warning("El listing %s ya está en el carrito del usuario %s", request.listingId, user_id)
            raise ItemAlreadyInCartException(f"El listing {request.listingId} ya está en tu carrito")

        # 4. Crear CartItem con snapshot de datos
        cart_item = CartItem(
            listing_id=listing_uuid,
            sneaker_sku=request.sneakerSku,
            size=request.size,
            price=Decimal(str(request.price)),
            brand=request.brand,
            color=request.color,
            condition=request.condition,
            cover_image=request.coverImage,
        )

        # 5. Agregar al carrito
        cart.add_item(cart_item)

        # 6. Guardar
        await self._cart_repository.save(cart)

        logger.info("Item %s agregado al carrito del usuario %s", request.listingId, user_id)
        return cart


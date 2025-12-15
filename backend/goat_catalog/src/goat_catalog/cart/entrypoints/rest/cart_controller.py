"""Controller REST para operaciones del carrito."""

import sys
from pathlib import Path
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status

from ...application.dto.add_item_request import AddItemRequest
from ...application.dto.cart_item_response import CartItemResponse
from ...application.dto.cart_response import CartResponse
from ...application.use_cases.add_item_to_cart_use_case import AddItemToCartUseCase
from ...application.use_cases.clear_cart_use_case import ClearCartUseCase
from ...application.use_cases.get_cart_use_case import GetCartUseCase
from ...application.use_cases.remove_item_from_cart_use_case import RemoveItemFromCartUseCase
from ...domain.exceptions import (
    CannotAddOwnListingException,
    CartNotFoundException,
    ItemAlreadyInCartException,
    ListingNotAvailableException,
)
from ...infrastructure.persistence.mongo_cart_repository import MongoCartRepository
from ...domain.repositories import CartRepository

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.dependencies import get_user_id_from_header

router = APIRouter(prefix="/api/cart", tags=["Cart"])


def _get_cart_repository() -> CartRepository:
    """Factory para obtener instancia de CartRepository."""
    return MongoCartRepository()


def _get_get_cart_use_case() -> GetCartUseCase:
    """Factory para obtener instancia de GetCartUseCase."""
    cart_repository = _get_cart_repository()
    return GetCartUseCase(cart_repository)


def _get_add_item_to_cart_use_case() -> AddItemToCartUseCase:
    """Factory para obtener instancia de AddItemToCartUseCase."""
    cart_repository = _get_cart_repository()
    return AddItemToCartUseCase(cart_repository)


def _get_remove_item_from_cart_use_case() -> RemoveItemFromCartUseCase:
    """Factory para obtener instancia de RemoveItemFromCartUseCase."""
    cart_repository = _get_cart_repository()
    return RemoveItemFromCartUseCase(cart_repository)


def _get_clear_cart_use_case() -> ClearCartUseCase:
    """Factory para obtener instancia de ClearCartUseCase."""
    cart_repository = _get_cart_repository()
    return ClearCartUseCase(cart_repository)


def _map_to_cart_response(cart) -> CartResponse:
    """Mapea Cart (dominio) a CartResponse (DTO)."""
    item_responses = [
        CartItemResponse(
            id=str(item.id),
            listingId=str(item.listing_id),
            sneakerSku=item.sneaker_sku,
            brand=item.brand,
            color=item.color,
            size=item.size,
            condition=item.condition,
            price=float(item.price),
            coverImage=item.cover_image,
            createdAt=item.added_at,
        )
        for item in cart.items
    ]

    return CartResponse(
        id=str(cart.id),
        userId=str(cart.user_id),
        items=item_responses,
        total=float(cart.calculate_total()),
        updatedAt=cart.updated_at,
    )


@router.get("", response_model=CartResponse, status_code=status.HTTP_200_OK)
async def get_cart(
    user_id: UUID = Depends(get_user_id_from_header),
) -> CartResponse:
    """Obtiene el carrito del usuario autenticado.
    
    GET /api/cart
    Headers:
        X-User-Id: <user_id>
    """
    use_case = _get_get_cart_use_case()
    cart = await use_case.execute(user_id)
    return _map_to_cart_response(cart)


@router.post("/items", response_model=CartResponse, status_code=status.HTTP_201_CREATED)
async def add_item_to_cart(
    request: AddItemRequest,
    user_id: UUID = Depends(get_user_id_from_header),
) -> CartResponse:
    """Agrega un item al carrito del usuario autenticado.
    
    POST /api/cart/items
    Headers:
        X-User-Id: <user_id>
    Body:
        {
          "listingId": "...",
          "sneakerSku": "...",
          "size": "...",
          "price": 150000.00,
          "brand": "...",
          "color": "...",
          "condition": "...",
          "coverImage": "..."
        }
    """
    use_case = _get_add_item_to_cart_use_case()
    try:
        cart = await use_case.execute(user_id, request)
        return _map_to_cart_response(cart)
    except ItemAlreadyInCartException as e:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(e),
        )
    except CannotAddOwnListingException as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=str(e),
        )
    except ListingNotAvailableException as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        )


@router.delete("/items/{item_id}", response_model=CartResponse, status_code=status.HTTP_200_OK)
async def remove_item_from_cart(
    item_id: str,
    user_id: UUID = Depends(get_user_id_from_header),
) -> CartResponse:
    """Remueve un item del carrito del usuario autenticado.
    
    DELETE /api/cart/items/{item_id}
    Headers:
        X-User-Id: <user_id>
    """
    try:
        item_uuid = UUID(item_id)
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="ID de item inválido",
        )

    use_case = _get_remove_item_from_cart_use_case()
    try:
        await use_case.execute(user_id, item_uuid)
    except CartNotFoundException as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(e),
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(e),
        )

    # Retornar el carrito actualizado
    get_cart_use_case = _get_get_cart_use_case()
    cart = await get_cart_use_case.execute(user_id)
    return _map_to_cart_response(cart)


@router.delete("", status_code=status.HTTP_200_OK)
async def clear_cart(
    user_id: UUID = Depends(get_user_id_from_header),
) -> dict:
    """Vacía el carrito del usuario autenticado.
    
    DELETE /api/cart
    Headers:
        X-User-Id: <user_id>
    """
    use_case = _get_clear_cart_use_case()
    await use_case.execute(user_id)
    return {"message": "Carrito vaciado exitosamente"}

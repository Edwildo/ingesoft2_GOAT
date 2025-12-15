"""Documento MongoDB para Cart."""

from datetime import datetime
from decimal import Decimal
from typing import List
from uuid import UUID

from pydantic import BaseModel, Field


class CartItemDocument(BaseModel):
    """Documento MongoDB que representa un item del carrito."""

    id: str = Field(..., description="ID del item")
    listing_id: str = Field(..., description="ID del listing")
    sneaker_sku: str = Field(..., description="SKU del sneaker")
    size: str = Field(..., description="Talla")
    price: str = Field(..., description="Precio como string (Decimal serializado)")
    brand: str = Field(..., description="Marca")
    color: str = Field(..., description="Color")
    condition: str = Field(..., description="Condición")
    cover_image: str | None = Field(None, description="URL de imagen de portada")
    added_at: datetime = Field(..., description="Fecha en que se agregó")

    @classmethod
    def from_entity(cls, cart_item) -> "CartItemDocument":
        """Crea un documento desde una entidad CartItem."""
        from ...domain.entities.cart_item import CartItem

        if not isinstance(cart_item, CartItem):
            raise ValueError("Debe ser una instancia de CartItem")

        return cls(
            id=str(cart_item.id),
            listing_id=str(cart_item.listing_id),
            sneaker_sku=cart_item.sneaker_sku,
            size=cart_item.size,
            price=str(cart_item.price),
            brand=cart_item.brand,
            color=cart_item.color,
            condition=cart_item.condition,
            cover_image=cart_item.cover_image,
            added_at=cart_item.added_at,
        )

    def to_entity(self):
        """Convierte el documento a una entidad CartItem."""
        from ...domain.entities.cart_item import CartItem
        from decimal import Decimal

        return CartItem(
            listing_id=UUID(self.listing_id),
            sneaker_sku=self.sneaker_sku,
            size=self.size,
            price=Decimal(self.price),
            brand=self.brand,
            color=self.color,
            condition=self.condition,
            cover_image=self.cover_image,
            item_id=UUID(self.id),
            added_at=self.added_at,
        )


class CartDocument(BaseModel):
    """Documento MongoDB que representa un carrito."""

    id: str = Field(..., description="ID del carrito")
    user_id: str = Field(..., description="ID del usuario propietario")
    items: List[CartItemDocument] = Field(default_factory=list, description="Items del carrito")
    created_at: datetime = Field(..., description="Fecha de creación")
    updated_at: datetime = Field(..., description="Fecha de última actualización")
    expire_at: datetime = Field(..., description="Fecha de expiración (TTL)")

    model_config = {
        "json_schema_extra": {
            "examples": [{}]
        },
    }

    @classmethod
    def from_entity(cls, cart) -> "CartDocument":
        """Crea un documento desde una entidad Cart."""
        from ...domain.entities.cart import Cart

        if not isinstance(cart, Cart):
            raise ValueError("Debe ser una instancia de Cart")

        return cls(
            id=str(cart.id),
            user_id=str(cart.user_id),
            items=[CartItemDocument.from_entity(item) for item in cart.items],
            created_at=cart.created_at,
            updated_at=cart.updated_at,
            expire_at=cart.expire_at,
        )

    def to_entity(self):
        """Convierte el documento a una entidad Cart."""
        from ...domain.entities.cart import Cart

        return Cart(
            user_id=UUID(self.user_id),
            items=[item.to_entity() for item in self.items],
            cart_id=UUID(self.id),
            created_at=self.created_at,
            updated_at=self.updated_at,
            expire_at=self.expire_at,
        )

    def to_dict(self) -> dict:
        """Convierte a diccionario para MongoDB (con _id en lugar de id)."""
        data = self.model_dump()
        data["_id"] = data.pop("id")
        return data

    @classmethod
    def from_dict(cls, data: dict) -> "CartDocument":
        """Crea desde un diccionario de MongoDB (convierte _id a id)."""
        if "_id" in data:
            data["id"] = data.pop("_id")
        return cls(**data)


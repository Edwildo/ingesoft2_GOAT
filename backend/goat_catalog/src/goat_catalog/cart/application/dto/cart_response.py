"""DTO de respuesta para Cart."""

from datetime import datetime

from pydantic import BaseModel, Field, field_serializer

from .cart_item_response import CartItemResponse


class CartResponse(BaseModel):
    """DTO de respuesta para un carrito.
    
    Compatible con el frontend TypeScript:
    interface Cart {
      id: string;
      userId: string;
      items: CartItem[];
      total: number;
      updatedAt?: string;
    }
    """

    id: str = Field(..., description="ID del carrito")
    userId: str = Field(..., description="ID del usuario")
    items: list[CartItemResponse] = Field(default_factory=list, description="Items del carrito")
    total: float = Field(..., description="Total del carrito")
    updatedAt: datetime | None = Field(None, description="Fecha de última actualización")

    @field_serializer("updatedAt")
    def serialize_updated_at(self, value: datetime | None) -> str | None:
        """Serializa datetime a string ISO."""
        return value.isoformat() if value else None

    model_config = {
        "json_schema_extra": {"examples": [{}]},
    }


"""DTO de respuesta para CartItem."""

from datetime import datetime

from pydantic import BaseModel, Field, field_serializer


class CartItemResponse(BaseModel):
    """DTO de respuesta para un item del carrito.
    
    Compatible con el frontend TypeScript y Java:
    interface CartItem {
      id: string;
      listingId: string;
      sneakerSku: string;
      brand: string;
      color: string;
      size: string;
      condition: string;
      price: number;
      coverImage?: string;
      createdAt: string;
    }
    """

    id: str = Field(..., description="ID del item")
    listingId: str = Field(..., description="ID del listing")
    sneakerSku: str = Field(..., description="SKU del sneaker")
    brand: str = Field(..., description="Marca")
    color: str = Field(..., description="Color")
    size: str = Field(..., description="Talla")
    condition: str = Field(..., description="Condición del sneaker")
    price: float = Field(..., description="Precio")
    coverImage: str | None = Field(None, description="URL de imagen de portada")
    createdAt: datetime = Field(..., description="Fecha en que se agregó")

    @field_serializer("createdAt")
    def serialize_created_at(self, value: datetime) -> str:
        """Serializa datetime a string ISO."""
        return value.isoformat()

    model_config = {
        "json_schema_extra": {"examples": [{}]},
    }

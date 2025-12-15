"""DTO de request para agregar item al carrito."""

from decimal import Decimal

from pydantic import BaseModel, Field


class AddItemRequest(BaseModel):
    """DTO de request para agregar un item al carrito.
    
    Java (orquestador) envía este request después de validar el listing.
    Incluye toda la información necesaria del listing ya validada.
    
    Ejemplo de uso:
    POST /api/cart/items
    X-User-Id: 550e8400-e29b-41d4-a716-446655440000
    Content-Type: application/json
    
    {
      "listingId": "123e4567-e89b-12d3-a456-426614174000",
      "sneakerSku": "NIKE-AIR-MAX-90-001",
      "size": "42",
      "price": 150000.00,
      "brand": "Nike",
      "color": "Black/White",
      "condition": "NEW",
      "coverImage": "https://..."
    }
    """

    listingId: str = Field(..., description="ID del listing a agregar")
    sneakerSku: str = Field(..., description="SKU del sneaker")
    size: str = Field(..., description="Talla del sneaker")
    price: Decimal = Field(..., description="Precio del listing (snapshot)")
    brand: str = Field(..., description="Marca del sneaker")
    color: str = Field(..., description="Color del sneaker")
    condition: str = Field(..., description="Condición del sneaker")
    coverImage: str | None = Field(None, description="URL de la imagen de portada")

    model_config = {
        "json_schema_extra": {
            "examples": [{
                "listingId": "123e4567-e89b-12d3-a456-426614174000",
                "sneakerSku": "NIKE-AIR-MAX-90-001",
                "size": "42",
                "price": 150000.00,
                "brand": "Nike",
                "color": "Black/White",
                "condition": "NEW",
                "coverImage": "https://example.com/image.jpg"
            }]
        },
    }


"""DTO para respuesta de Sneaker."""

from typing import List, Optional

from pydantic import BaseModel, Field


class MediaResponse(BaseModel):
    """DTO para información de medios."""

    cover_image: Optional[str] = Field(
        None, description="URL de la imagen de portada", examples=["https://example.com/image.jpg"]
    )
    gallery: List[str] = Field(
        default_factory=list,
        description="Lista de URLs de imágenes adicionales",
        examples=[["https://example.com/img1.jpg", "https://example.com/img2.jpg"]],
    )


class SneakerResponse(BaseModel):
    """DTO para respuesta de un sneaker."""

    sku: str = Field(..., description="SKU único del sneaker", examples=["NIKE-AIR-JORDAN-1-001"])
    brand: str = Field(..., description="Marca del sneaker", examples=["Nike"])
    model: str = Field(..., description="Modelo del sneaker", examples=["Air Jordan 1"])
    gender: str = Field(..., description="Género", examples=["UNISEX"])
    description: Optional[str] = Field(
        None, description="Descripción del sneaker", examples=["Classic basketball sneaker"]
    )
    categories: List[str] = Field(
        default_factory=list, description="IDs de categorías", examples=[["cat1", "cat2"]]
    )
    collections: List[str] = Field(
        default_factory=list, description="IDs de colecciones", examples=[["coll1"]]
    )
    media: MediaResponse = Field(
        default_factory=lambda: MediaResponse(),
        description="Información de medios (imágenes)",
    )

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "sku": "NIKE-AIR-JORDAN-1-001",
                    "brand": "Nike",
                    "model": "Air Jordan 1",
                    "gender": "UNISEX",
                    "description": "Classic basketball sneaker",
                    "categories": ["basketball", "sports"],
                    "collections": ["jordan-collection"],
                    "media": {
                        "cover_image": "https://example.com/cover.jpg",
                        "gallery": ["https://example.com/img1.jpg"],
                    },
                }
            ]
        }
    }


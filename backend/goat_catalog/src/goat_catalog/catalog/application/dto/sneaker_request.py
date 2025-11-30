"""DTO para request de creación/actualización de Sneaker."""

from typing import List, Optional

from pydantic import BaseModel, Field


class MediaRequest(BaseModel):
    """DTO para información de medios en request."""

    cover_image: Optional[str] = Field(
        None, description="URL de la imagen de portada", examples=["https://example.com/image.jpg"]
    )
    gallery: Optional[List[str]] = Field(
        None,
        description="Lista de URLs de imágenes adicionales",
        examples=[["https://example.com/img1.jpg"]],
    )


class SneakerRequest(BaseModel):
    """DTO para crear o actualizar un sneaker."""

    sku: str = Field(..., description="SKU único del sneaker", examples=["NIKE-AIR-JORDAN-1-001"])
    brand: str = Field(..., description="ID o nombre de la marca", examples=["Nike"])
    model: str = Field(..., description="Modelo del sneaker", examples=["Air Jordan 1"])
    gender: str = Field(..., description="Género (MALE, FEMALE, UNISEX)", examples=["UNISEX"])
    description: Optional[str] = Field(
        None, description="Descripción del sneaker", examples=["Classic basketball sneaker"]
    )
    categories: Optional[List[str]] = Field(
        None, description="IDs de categorías", examples=[["basketball", "sports"]]
    )
    collections: Optional[List[str]] = Field(
        None, description="IDs de colecciones", examples=[["jordan-collection"]]
    )
    media: Optional[MediaRequest] = Field(
        None, description="Información de medios (imágenes)"
    )

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "sku": "NIKE-AIR-JORDAN-1-001",
                    "brand": "nike",
                    "model": "Air Jordan 1",
                    "gender": "UNISEX",
                    "description": "Classic basketball sneaker",
                    "categories": ["basketball"],
                    "collections": ["jordan-collection"],
                    "media": {
                        "cover_image": "https://example.com/cover.jpg",
                        "gallery": ["https://example.com/img1.jpg"],
                    },
                }
            ]
        }
    }


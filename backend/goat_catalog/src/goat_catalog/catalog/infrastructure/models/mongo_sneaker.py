"""Documento MongoDB para Sneakers."""

from typing import List, Optional

from pydantic import BaseModel, Field


class MongoMedia(BaseModel):
    """Documento MongoDB para información de medios."""

    cover_image: Optional[str] = None
    gallery: List[str] = Field(default_factory=list)


class MongoSneaker(BaseModel):
    """Documento MongoDB que representa un Sneaker."""

    sku: str = Field(..., description="SKU único del sneaker")
    brand: str = Field(..., description="ID o nombre de la marca")
    model: str = Field(..., description="Modelo del sneaker")
    gender: str = Field(..., description="Género (MALE, FEMALE, UNISEX)")
    description: Optional[str] = Field(None, description="Descripción del sneaker")
    categories: List[str] = Field(default_factory=list, description="IDs de categorías")
    collections: List[str] = Field(default_factory=list, description="IDs de colecciones")
    media: MongoMedia = Field(default_factory=lambda: MongoMedia(), description="Información de medios")

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

    @classmethod
    def from_entity(cls, sneaker) -> "MongoSneaker":
        """Crea un documento desde una entidad de dominio."""
        from ...domain.entities.sneaker import Sneaker

        if not isinstance(sneaker, Sneaker):
            raise ValueError("Debe ser una instancia de Sneaker")

        media = sneaker.media
        mongo_media = MongoMedia(
            cover_image=media.cover_image,
            gallery=media.gallery,
        )

        return cls(
            sku=sneaker.sku.value,
            brand=sneaker.brand,
            model=sneaker.model,
            gender=sneaker.gender,
            description=sneaker.description,
            categories=sneaker.categories,
            collections=sneaker.collections,
            media=mongo_media,
        )

    def to_entity(self):
        """Convierte el documento a una entidad de dominio."""
        from ...domain.entities.sneaker import Sneaker
        from ...domain.value_objects.media import Media
        from ...domain.value_objects.sku import SKU

        media_vo = Media(
            cover_image=self.media.cover_image,
            gallery=self.media.gallery,
        )

        return Sneaker(
            sku=SKU(self.sku),
            brand=self.brand,
            model=self.model,
            gender=self.gender,
            description=self.description,
            categories=self.categories,
            collections=self.collections,
            media=media_vo,
        )


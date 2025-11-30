"""Documento MongoDB para Categories."""

from typing import Optional

from pydantic import BaseModel, Field


class MongoCategory(BaseModel):
    """Documento MongoDB que representa una Category."""

    id: str = Field(..., description="ID único de la categoría", alias="_id")
    name: str = Field(..., description="Nombre de la categoría")
    slug: str = Field(..., description="Slug de la categoría (URL-friendly)")
    description: Optional[str] = Field(None, description="Descripción de la categoría")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "_id": "basketball",
                    "name": "Basketball",
                    "slug": "basketball",
                    "description": "Basketball shoes",
                }
            ]
        },
        "populate_by_name": True,
    }

    @classmethod
    def from_entity(cls, category) -> "MongoCategory":
        """Crea un documento desde una entidad de dominio."""
        from ...domain.entities.category import Category

        if not isinstance(category, Category):
            raise ValueError("Debe ser una instancia de Category")

        return cls(
            id=category.id,
            name=category.name,
            slug=category.slug,
            description=category.description,
        )

    def to_entity(self):
        """Convierte el documento a una entidad de dominio."""
        from ...domain.entities.category import Category

        return Category(
            id=self.id,
            name=self.name,
            slug=self.slug,
            description=self.description,
        )


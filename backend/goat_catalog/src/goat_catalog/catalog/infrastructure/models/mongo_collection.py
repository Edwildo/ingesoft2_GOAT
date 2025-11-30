"""Documento MongoDB para Collections."""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class MongoCollection(BaseModel):
    """Documento MongoDB que representa una Collection."""

    id: str = Field(..., description="ID único de la colección", alias="_id")
    name: str = Field(..., description="Nombre de la colección")
    slug: str = Field(..., description="Slug de la colección (URL-friendly)")
    description: Optional[str] = Field(None, description="Descripción de la colección")
    release_date: Optional[datetime] = Field(None, description="Fecha de lanzamiento")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "_id": "jordan-collection",
                    "name": "Jordan Collection",
                    "slug": "jordan-collection",
                    "description": "Classic Jordan sneakers",
                    "release_date": "2024-01-01T00:00:00",
                }
            ]
        },
        "populate_by_name": True,
    }

    @classmethod
    def from_entity(cls, collection) -> "MongoCollection":
        """Crea un documento desde una entidad de dominio."""
        from ...domain.entities.collection import Collection

        if not isinstance(collection, Collection):
            raise ValueError("Debe ser una instancia de Collection")

        return cls(
            id=collection.id,
            name=collection.name,
            slug=collection.slug,
            description=collection.description,
            release_date=collection.release_date,
        )

    def to_entity(self):
        """Convierte el documento a una entidad de dominio."""
        from ...domain.entities.collection import Collection

        return Collection(
            id=self.id,
            name=self.name,
            slug=self.slug,
            description=self.description,
            release_date=self.release_date,
        )


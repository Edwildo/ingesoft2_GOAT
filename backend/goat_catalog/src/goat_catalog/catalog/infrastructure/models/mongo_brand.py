"""Documento MongoDB para Brands."""

from typing import Optional

from pydantic import BaseModel, Field


class MongoBrand(BaseModel):
    """Documento MongoDB que representa una Brand."""

    id: str = Field(..., description="ID único de la marca", alias="_id")
    name: str = Field(..., description="Nombre de la marca")
    slug: str = Field(..., description="Slug de la marca (URL-friendly)")
    logo_url: Optional[str] = Field(None, description="URL del logo de la marca")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "_id": "nike",
                    "name": "Nike",
                    "slug": "nike",
                    "logo_url": "https://example.com/nike-logo.png",
                }
            ]
        },
        "populate_by_name": True,
    }

    @classmethod
    def from_entity(cls, brand) -> "MongoBrand":
        """Crea un documento desde una entidad de dominio."""
        from ...domain.entities.brand import Brand

        if not isinstance(brand, Brand):
            raise ValueError("Debe ser una instancia de Brand")

        return cls(
            id=brand.id,
            name=brand.name,
            slug=brand.slug.value,
            logo_url=brand.logo_url,
        )

    def to_entity(self):
        """Convierte el documento a una entidad de dominio."""
        from ...domain.entities.brand import Brand
        from ...domain.value_objects.brand_slug import BrandSlug

        return Brand(
            id=self.id,
            name=self.name,
            slug=BrandSlug(self.slug),
            logo_url=self.logo_url,
        )


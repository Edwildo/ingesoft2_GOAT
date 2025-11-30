"""DTO para respuesta de Brand."""

from typing import Optional

from pydantic import BaseModel, Field


class BrandResponse(BaseModel):
    """DTO para respuesta de una marca."""

    id: str = Field(..., description="ID único de la marca", examples=["nike"])
    name: str = Field(..., description="Nombre de la marca", examples=["Nike"])
    slug: str = Field(..., description="Slug de la marca (URL-friendly)", examples=["nike"])
    logo_url: Optional[str] = Field(
        None, description="URL del logo de la marca", examples=["https://example.com/nike-logo.png"]
    )

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "id": "nike",
                    "name": "Nike",
                    "slug": "nike",
                    "logo_url": "https://example.com/nike-logo.png",
                }
            ]
        }
    }


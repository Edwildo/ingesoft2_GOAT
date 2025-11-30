"""DTO para respuesta de Category."""

from typing import Optional

from pydantic import BaseModel, Field


class CategoryResponse(BaseModel):
    """DTO para respuesta de una categoría."""

    id: str = Field(..., description="ID único de la categoría", examples=["basketball"])
    name: str = Field(..., description="Nombre de la categoría", examples=["Basketball"])
    slug: str = Field(..., description="Slug de la categoría (URL-friendly)", examples=["basketball"])
    description: Optional[str] = Field(
        None, description="Descripción de la categoría", examples=["Basketball shoes"]
    )

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "id": "basketball",
                    "name": "Basketball",
                    "slug": "basketball",
                    "description": "Basketball shoes",
                }
            ]
        }
    }


"""DTO para respuesta de Collection."""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class CollectionResponse(BaseModel):
    """DTO para respuesta de una colección."""

    id: str = Field(..., description="ID único de la colección", examples=["jordan-collection"])
    name: str = Field(..., description="Nombre de la colección", examples=["Jordan Collection"])
    slug: str = Field(
        ..., description="Slug de la colección (URL-friendly)", examples=["jordan-collection"]
    )
    description: Optional[str] = Field(
        None, description="Descripción de la colección", examples=["Classic Jordan sneakers"]
    )
    release_date: Optional[datetime] = Field(
        None, description="Fecha de lanzamiento", examples=["2024-01-01T00:00:00"]
    )

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "id": "jordan-collection",
                    "name": "Jordan Collection",
                    "slug": "jordan-collection",
                    "description": "Classic Jordan sneakers",
                    "release_date": "2024-01-01T00:00:00",
                }
            ]
        }
    }


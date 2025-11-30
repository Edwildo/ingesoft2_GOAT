"""Implementación MongoDB del repositorio de Brands."""

import sys
from pathlib import Path
from typing import List, Optional

from motor.motor_asyncio import AsyncIOMotorDatabase
from pymongo.collection import Collection

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.config.database import get_database
from ...domain.entities.brand import Brand
from ...domain.repositories.brand_repository import BrandRepository
from ..models.mongo_brand import MongoBrand


class MongoBrandRepository(BrandRepository):
    """Implementación MongoDB del repositorio de Brands."""

    COLLECTION_NAME = "catalog.brands"

    def __init__(self, database: Optional[AsyncIOMotorDatabase] = None) -> None:
        """Inicializa el repositorio MongoDB.

        Args:
            database: Instancia de la base de datos MongoDB
        """
        self._database = database or get_database()
        self._collection: Collection = self._database[self.COLLECTION_NAME]

    async def find_by_id(self, brand_id: str) -> Optional[Brand]:
        """Busca una marca por su ID."""
        document_dict = await self._collection.find_one({"_id": brand_id})

        if not document_dict:
            return None

        # Usar model_validate para manejar correctamente el alias _id
        document = MongoBrand.model_validate(document_dict)
        return document.to_entity()

    async def find_all(self) -> List[Brand]:
        """Retorna todas las marcas."""
        cursor = self._collection.find({}).sort("name", 1)
        brands = []

        async for document_dict in cursor:
            document = MongoBrand.model_validate(document_dict)
            brands.append(document.to_entity())

        return brands

    async def find_by_slug(self, slug: str) -> Optional[Brand]:
        """Busca una marca por su slug."""
        document_dict = await self._collection.find_one({"slug": slug})

        if not document_dict:
            return None

        document = MongoBrand.model_validate(document_dict)
        return document.to_entity()

    async def save(self, brand: Brand) -> None:
        """Guarda una marca en MongoDB."""
        document = MongoBrand.from_entity(brand)
        document_dict = document.model_dump(by_alias=True)

        await self._collection.update_one(
            {"_id": document_dict["_id"]},
            {"$set": document_dict},
            upsert=True,
        )


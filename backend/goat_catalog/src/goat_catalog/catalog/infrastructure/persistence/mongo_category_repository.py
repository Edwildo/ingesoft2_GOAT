"""Implementación MongoDB del repositorio de Categories."""

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
from ...domain.entities.category import Category
from ...domain.repositories.category_repository import CategoryRepository
from ..models.mongo_category import MongoCategory


class MongoCategoryRepository(CategoryRepository):
    """Implementación MongoDB del repositorio de Categories."""

    COLLECTION_NAME = "catalog.categories"

    def __init__(self, database: Optional[AsyncIOMotorDatabase] = None) -> None:
        """Inicializa el repositorio MongoDB.

        Args:
            database: Instancia de la base de datos MongoDB
        """
        self._database = database or get_database()
        self._collection: Collection = self._database[self.COLLECTION_NAME]

    async def find_by_id(self, category_id: str) -> Optional[Category]:
        """Busca una categoría por su ID."""
        document_dict = await self._collection.find_one({"_id": category_id})

        if not document_dict:
            return None

        document = MongoCategory.model_validate(document_dict)
        return document.to_entity()

    async def find_all(self) -> List[Category]:
        """Retorna todas las categorías."""
        cursor = self._collection.find({}).sort("name", 1)
        categories = []

        async for document_dict in cursor:
            document = MongoCategory(**document_dict)
            categories.append(document.to_entity())

        return categories

    async def find_by_slug(self, slug: str) -> Optional[Category]:
        """Busca una categoría por su slug."""
        document_dict = await self._collection.find_one({"slug": slug})

        if not document_dict:
            return None

        document = MongoCategory.model_validate(document_dict)
        return document.to_entity()

    async def exists_by_id(self, category_id: str) -> bool:
        """Verifica si existe una categoría con el ID dado."""
        count = await self._collection.count_documents({"_id": category_id}, limit=1)
        return count > 0

    async def save(self, category: Category) -> None:
        """Guarda una categoría en MongoDB."""
        document = MongoCategory.from_entity(category)
        document_dict = document.model_dump(by_alias=True)

        await self._collection.update_one(
            {"_id": document_dict["_id"]},
            {"$set": document_dict},
            upsert=True,
        )


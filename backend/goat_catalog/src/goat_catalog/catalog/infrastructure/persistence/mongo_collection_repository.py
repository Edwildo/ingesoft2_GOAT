"""Implementación MongoDB del repositorio de Collections."""

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
from ...domain.entities.collection import Collection as CollectionEntity
from ...domain.repositories.collection_repository import CollectionRepository
from ..models.mongo_collection import MongoCollection


class MongoCollectionRepository(CollectionRepository):
    """Implementación MongoDB del repositorio de Collections."""

    COLLECTION_NAME = "catalog.collections"

    def __init__(self, database: Optional[AsyncIOMotorDatabase] = None) -> None:
        """Inicializa el repositorio MongoDB.

        Args:
            database: Instancia de la base de datos MongoDB
        """
        self._database = database or get_database()
        self._collection: Collection = self._database[self.COLLECTION_NAME]

    async def find_by_id(self, collection_id: str) -> Optional[CollectionEntity]:
        """Busca una colección por su ID."""
        document_dict = await self._collection.find_one({"_id": collection_id})

        if not document_dict:
            return None

        document = MongoCollection.model_validate(document_dict)
        return document.to_entity()

    async def find_all(self) -> List[CollectionEntity]:
        """Retorna todas las colecciones."""
        cursor = self._collection.find({}).sort("name", 1)
        collections = []

        async for document_dict in cursor:
            document = MongoCollection(**document_dict)
            collections.append(document.to_entity())

        return collections

    async def exists_by_id(self, collection_id: str) -> bool:
        """Verifica si existe una colección con el ID dado."""
        count = await self._collection.count_documents({"_id": collection_id}, limit=1)
        return count > 0

    async def save(self, collection: CollectionEntity) -> None:
        """Guarda una colección en MongoDB."""
        document = MongoCollection.from_entity(collection)
        document_dict = document.model_dump(by_alias=True)

        await self._collection.update_one(
            {"_id": document_dict["_id"]},
            {"$set": document_dict},
            upsert=True,
        )


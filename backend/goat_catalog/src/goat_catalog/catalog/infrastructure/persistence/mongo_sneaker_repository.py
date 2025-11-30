"""Implementación MongoDB del repositorio de Sneakers."""

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
from ...domain.entities.sneaker import Sneaker
from ...domain.exceptions.duplicate_sku_exception import DuplicateSkuException
from ...domain.repositories.sneaker_repository import SneakerRepository
from ...domain.value_objects.sku import SKU
from ..models.mongo_sneaker import MongoSneaker


class MongoSneakerRepository(SneakerRepository):
    """Implementación MongoDB del repositorio de Sneakers."""

    COLLECTION_NAME = "catalog.sneakers"

    def __init__(self, database: Optional[AsyncIOMotorDatabase] = None) -> None:
        """Inicializa el repositorio MongoDB.

        Args:
            database: Instancia de la base de datos MongoDB
        """
        self._database = database or get_database()
        self._collection: Collection = self._database[self.COLLECTION_NAME]

    async def find_by_sku(self, sku: SKU) -> Optional[Sneaker]:
        """Busca un sneaker por su SKU."""
        document_dict = await self._collection.find_one({"sku": sku.value})

        if not document_dict:
            return None

        document = MongoSneaker(**document_dict)
        return document.to_entity()

    async def exists_by_sku(self, sku: SKU) -> bool:
        """Verifica si existe un sneaker con el SKU dado."""
        count = await self._collection.count_documents({"sku": sku.value}, limit=1)
        return count > 0

    async def search(
        self,
        brand: Optional[str] = None,
        category: Optional[str] = None,
        gender: Optional[str] = None,
        collection: Optional[str] = None,
        search_text: Optional[str] = None,
        page: int = 1,
        size: int = 20,
    ) -> tuple[List[Sneaker], int]:
        """Busca sneakers con filtros opcionales."""
        # Construir query de filtros
        query = self._build_search_query(
            brand=brand,
            category=category,
            gender=gender,
            collection=collection,
            search_text=search_text,
        )

        # Contar total de resultados
        total = await self._collection.count_documents(query)

        # Calcular skip para paginación
        skip = (page - 1) * size

        # Buscar con paginación
        cursor = (
            self._collection.find(query)
            .sort("model", 1)
            .skip(skip)
            .limit(size)
        )

        sneakers = []
        async for document_dict in cursor:
            document = MongoSneaker(**document_dict)
            sneakers.append(document.to_entity())

        return sneakers, total

    def _build_search_query(
        self,
        brand: Optional[str] = None,
        category: Optional[str] = None,
        gender: Optional[str] = None,
        collection: Optional[str] = None,
        search_text: Optional[str] = None,
    ) -> dict:
        """Construye la query de búsqueda con filtros opcionales."""
        query = {}

        if brand:
            query["brand"] = {"$regex": brand, "$options": "i"}

        if category:
            query["categories"] = category

        if gender:
            query["gender"] = gender.upper()

        if collection:
            query["collections"] = collection

        if search_text:
            # Búsqueda de texto en modelo y descripción
            query["$or"] = [
                {"model": {"$regex": search_text, "$options": "i"}},
                {"description": {"$regex": search_text, "$options": "i"}},
            ]

        return query

    async def save(self, sneaker: Sneaker) -> None:
        """Guarda un sneaker en MongoDB.
        
        Raises:
            DuplicateSkuException: Si el SKU ya existe al crear uno nuevo
        """
        document = MongoSneaker.from_entity(sneaker)
        document_dict = document.model_dump()

        # Usar sku como _id para garantizar unicidad
        document_dict["_id"] = document_dict["sku"]

        # Usar upsert para crear o actualizar
        await self._collection.update_one(
            {"_id": document_dict["_id"]},
            {"$set": document_dict},
            upsert=True,
        )

    async def delete(self, sku: SKU) -> None:
        """Elimina un sneaker por su SKU."""
        await self._collection.delete_one({"sku": sku.value})


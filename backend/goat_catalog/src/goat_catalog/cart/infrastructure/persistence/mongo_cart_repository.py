"""Implementación MongoDB del repositorio de carritos."""

import logging
from typing import Optional
from uuid import UUID

from motor.motor_asyncio import AsyncIOMotorDatabase
from pymongo.collection import Collection

from ...domain.entities import Cart
from ...domain.repositories import CartRepository
from ..models.cart_document import CartDocument

logger = logging.getLogger(__name__)

# Agregar src al path para importaciones absolutas
import sys
from pathlib import Path

src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.config.database import get_database


class MongoCartRepository(CartRepository):
    """Implementación MongoDB del repositorio de carritos."""

    COLLECTION_NAME = "carts"

    def __init__(self, database: Optional[AsyncIOMotorDatabase] = None) -> None:
        """Inicializa el repositorio MongoDB.

        Args:
            database: Instancia de la base de datos MongoDB
        """
        self._database = database or get_database()
        self._collection: Collection = self._database[self.COLLECTION_NAME]

    async def find_active_by_user(self, user_id: UUID) -> Optional[Cart]:
        """Busca el carrito activo de un usuario.

        Args:
            user_id: ID del usuario

        Returns:
            Cart si existe, None en caso contrario
        """
        from datetime import datetime

        # Buscar carrito que no haya expirado
        filter_dict = {
            "user_id": str(user_id),
            "expire_at": {"$gt": datetime.utcnow()},  # Solo carritos no expirados
        }

        document_dict = await self._collection.find_one(filter_dict)

        if not document_dict:
            return None

        try:
            document = CartDocument.from_dict(document_dict)
            return document.to_entity()
        except Exception as e:
            logger.error("Error convirtiendo documento a entidad: %s", e)
            return None

    async def save(self, cart: Cart) -> None:
        """Guarda o actualiza un carrito.

        Args:
            cart: Carrito a guardar
        """
        document = CartDocument.from_entity(cart)
        document_dict = document.to_dict()

        # Extraer _id para usarlo como filtro
        cart_id = document_dict.pop("_id")

        # Usar replace_one para reemplazar completamente el documento
        # Esto evita problemas con campos que se eliminan
        await self._collection.replace_one(
            {"_id": cart_id},
            {"_id": cart_id, **document_dict},
            upsert=True,
        )

    async def delete(self, cart_id: UUID) -> None:
        """Elimina un carrito.

        Args:
            cart_id: ID del carrito a eliminar
        """
        filter_dict = {"_id": str(cart_id)}
        await self._collection.delete_one(filter_dict)


"""Implementación MongoDB del repositorio de emails confirmados."""

import sys
from pathlib import Path
from typing import Optional

from motor.motor_asyncio import AsyncIOMotorDatabase
from pymongo.collection import Collection

from ...domain.entities.confirmed_email import ConfirmedEmail
from ...domain.repositories.confirmed_email_repository import ConfirmedEmailRepository
from ...domain.value_objects.email import Email

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.config.database import get_database
from ..models.confirmed_email_document import ConfirmedEmailDocument


class MongoConfirmedEmailRepository(ConfirmedEmailRepository):
    """Implementación MongoDB del repositorio de emails confirmados."""

    COLLECTION_NAME = "confirmed_emails"

    def __init__(self, database: Optional[AsyncIOMotorDatabase] = None) -> None:
        """Inicializa el repositorio MongoDB.

        Args:
            database: Instancia de la base de datos MongoDB
        """
        self._database = database or get_database()
        self._collection: Collection = self._database[self.COLLECTION_NAME]

    async def save(self, confirmed_email: ConfirmedEmail) -> None:
        """Guarda un email confirmado en MongoDB."""
        document = ConfirmedEmailDocument.from_entity(confirmed_email)
        document_dict = document.model_dump()

        # Usar email como clave única
        filter_dict = {"email": document.email}

        await self._collection.update_one(
            filter_dict,
            {"$set": document_dict},
            upsert=True,
        )

    async def find_by_email(self, email: Email) -> Optional[ConfirmedEmail]:
        """Busca un email confirmado por email."""
        filter_dict = {"email": email.value}

        document_dict = await self._collection.find_one(filter_dict)

        if not document_dict:
            return None

        document = ConfirmedEmailDocument(**document_dict)
        return document.to_entity()

    async def is_email_confirmed(self, email: Email) -> bool:
        """Verifica si un email está confirmado."""
        confirmed_email = await self.find_by_email(email)
        return confirmed_email is not None


"""Implementación MongoDB del repositorio de OTPs."""

from typing import Optional

from motor.motor_asyncio import AsyncIOMotorDatabase
from pymongo.collection import Collection

from ...domain.entities.otp_token import OTPToken
from ...domain.repositories.otp_repository import OTPRepository
from ...domain.value_objects.email import Email
from ...domain.value_objects.otp_purpose import OTPPurpose
import sys
from pathlib import Path

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.config.database import get_database
from ..models.otp_document import OTPDocument


class MongoOTPRepository(OTPRepository):
    """Implementación MongoDB del repositorio de OTPs."""

    COLLECTION_NAME = "otps"

    def __init__(self, database: Optional[AsyncIOMotorDatabase] = None) -> None:
        """Inicializa el repositorio MongoDB.

        Args:
            database: Instancia de la base de datos MongoDB
        """
        self._database = database or get_database()
        self._collection: Collection = self._database[self.COLLECTION_NAME]

    async def save(self, otp_token: OTPToken) -> None:
        """Guarda un OTPToken en MongoDB."""
        document = OTPDocument.from_entity(otp_token)
        document_dict = document.model_dump()

        # Usar email + purpose como clave única
        filter_dict = {
            "email": document.email,
            "purpose": document.purpose,
        }

        await self._collection.update_one(
            filter_dict,
            {"$set": document_dict},
            upsert=True,
        )

    async def find_by_email_and_purpose(
        self, email: Email, purpose: OTPPurpose
    ) -> Optional[OTPToken]:
        """Busca un OTPToken por email y propósito."""
        filter_dict = {
            "email": email.value,
            "purpose": purpose.value,
        }

        document_dict = await self._collection.find_one(filter_dict)

        if not document_dict:
            return None

        document = OTPDocument(**document_dict)
        return document.to_entity()

    async def delete(self, otp_token: OTPToken) -> None:
        """Elimina un OTPToken de MongoDB."""
        filter_dict = {
            "email": otp_token.email.value,
            "purpose": otp_token.purpose.value,
        }

        await self._collection.delete_one(filter_dict)

    async def delete_by_email_and_purpose(
        self, email: Email, purpose: OTPPurpose
    ) -> None:
        """Elimina un OTPToken por email y propósito."""
        filter_dict = {
            "email": email.value,
            "purpose": purpose.value,
        }

        await self._collection.delete_one(filter_dict)


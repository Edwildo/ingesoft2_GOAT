"""Utilidades para inicialización de índices MongoDB."""

import logging
from typing import Any

from motor.motor_asyncio import AsyncIOMotorDatabase
from pymongo.collection import Collection
from pymongo.errors import OperationFailure

logger = logging.getLogger(__name__)


async def create_index_safe(
    collection: Collection,
    index_spec: Any,
    index_name: str,
    description: str,
    **kwargs: Any,
) -> bool:
    """Crea un índice de forma segura, manejando si ya existe.
    
    Args:
        collection: Colección de MongoDB
        index_spec: Especificación del índice (string, lista de tuplas, etc.)
        index_name: Nombre del índice
        description: Descripción del índice para logging
        **kwargs: Argumentos adicionales para create_index (unique, expireAfterSeconds, etc.)
        
    Returns:
        True si el índice fue creado o ya existía, False si hubo error
    """
    try:
        await collection.create_index(index_spec, name=index_name, **kwargs)
        logger.info(f"Índice creado: {description}")
        return True
    except OperationFailure as e:
        error_msg = str(e).lower()
        if "already exists" in error_msg or "duplicate key" in error_msg:
            logger.debug(f"Índice ya existe: {description}")
            return True
        logger.error(f"Error creando índice {description}: {e}")
        return False
    except Exception as e:
        logger.error(f"Error inesperado creando índice {description}: {type(e).__name__}: {e}")
        return False


async def initialize_otp_indexes(database: AsyncIOMotorDatabase) -> bool:
    """Inicializa todos los índices necesarios para la colección 'otps'.
    
    Args:
        database: Instancia de la base de datos MongoDB
        
    Returns:
        True si todos los índices se crearon correctamente, False en caso contrario
    """
    collection = database["otps"]
    indexes_created = []

    # Crear índice TTL en expire_at (los documentos se eliminarán automáticamente)
    success = await create_index_safe(
        collection=collection,
        index_spec="expire_at",
        index_name="expire_at_ttl",
        description="Índice TTL en 'expire_at' (eliminación automática de OTPs expirados)",
        expireAfterSeconds=0,
    )
    indexes_created.append(success)

    # Crear índice único compuesto en email + purpose
    success = await create_index_safe(
        collection=collection,
        index_spec=[("email", 1), ("purpose", 1)],
        index_name="email_purpose_unique",
        description="Índice único compuesto en 'email' + 'purpose'",
        unique=True,
    )
    indexes_created.append(success)

    # Crear índice en email para búsquedas rápidas
    success = await create_index_safe(
        collection=collection,
        index_spec="email",
        index_name="email_index",
        description="Índice en 'email' para búsquedas rápidas",
    )
    indexes_created.append(success)

    all_success = all(indexes_created)
    if all_success:
        logger.info("Todos los índices de OTP fueron inicializados correctamente")
    else:
        logger.warning("Algunos índices de OTP no pudieron ser creados")

    return all_success


async def verify_otp_indexes(database: AsyncIOMotorDatabase) -> dict[str, bool]:
    """Verifica que todos los índices necesarios existan en la colección 'otps'.
    
    Args:
        database: Instancia de la base de datos MongoDB
        
    Returns:
        Diccionario con el estado de cada índice (nombre -> existe)
    """
    collection = database["otps"]
    required_indexes = {
        "expire_at_ttl": "expire_at",
        "email_purpose_unique": [("email", 1), ("purpose", 1)],
        "email_index": "email",
    }
    
    try:
        existing_indexes = await collection.list_indexes().to_list(length=None)
        existing_names = {idx.get("name", "") for idx in existing_indexes}
        
        return {
            name: name in existing_names
            for name in required_indexes.keys()
        }
    except Exception as e:
        logger.error(f"Error verificando índices: {type(e).__name__}: {e}")
        return {name: False for name in required_indexes.keys()}


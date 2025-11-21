"""Script para inicializar índices TTL en MongoDB."""

import asyncio
import sys
from pathlib import Path

# Agregar src al path
sys.path.insert(0, str(Path(__file__).parent.parent / "src"))

from motor.motor_asyncio import AsyncIOMotorClient
from pymongo.errors import CollectionInvalid, OperationFailure
from pymongo.server_api import ServerApi

from goat_catalog.shared.config import get_settings


async def init_indexes() -> None:
    """Inicializa los índices TTL en MongoDB (local o Atlas)."""
    settings = get_settings()

    print(f"Conectando a MongoDB: {settings.mongodb_database}")
    print(f"URI: {settings.mongodb_uri[:50]}...\n")

    is_atlas = settings.mongodb_uri.startswith("mongodb+srv://")

    try:
        if is_atlas:
            print("Detectado MongoDB Atlas")
            client = AsyncIOMotorClient(
                settings.mongodb_uri,
                server_api=ServerApi("1"),
                serverSelectionTimeoutMS=10000, 
            )
        else:
            print("Detectado MongoDB local")
            client = AsyncIOMotorClient(
                settings.mongodb_uri,
                serverSelectionTimeoutMS=5000,
            )

        await client.admin.command("ping")
        print("Conexión establecida\n")

        database = client[settings.mongodb_database]
        collection = database["otps"]

        print("Inicializando colección 'otps'...\n")

        # Crear índice TTL en expire_at (los documentos se eliminarán automáticamente)
        try:
            await collection.create_index(
                "expire_at",
                expireAfterSeconds=0,
                name="expire_at_ttl",
            )
            print("✓ Índice TTL creado en 'expire_at' (eliminación automática de OTPs expirados)")
        except OperationFailure as e:
            if "already exists" in str(e).lower() or "duplicate key" in str(e).lower():
                print("✓ Índice TTL 'expire_at' ya existe")
            else:
                raise

        # Crear índice único compuesto en email + purpose
        try:
            await collection.create_index(
                [("email", 1), ("purpose", 1)],
                unique=True,
                name="email_purpose_unique",
            )
            print("✓ Índice único compuesto creado en 'email' + 'purpose'")
        except OperationFailure as e:
            if "already exists" in str(e).lower() or "duplicate key" in str(e).lower():
                print("✓ Índice único 'email' + 'purpose' ya existe")
            else:
                raise

        # Crear índice en email para búsquedas rápidas
        try:
            await collection.create_index(
                "email",
                name="email_index",
            )
            print("✓ Índice creado en 'email' para búsquedas rápidas")
        except OperationFailure as e:
            if "already exists" in str(e).lower() or "duplicate key" in str(e).lower():
                print("✓ Índice 'email' ya existe")
            else:
                raise

        print("Todos los índices fueron inicializados correctamente")

    except OperationFailure as e:
        error_msg = str(e)
        if "authentication failed" in error_msg.lower():
            print(f"Error de autenticación: Verifica las credenciales en .env")
        elif "timeout" in error_msg.lower():
            print(f"Error de conexión: Timeout al conectar. Verifica tu conexión a internet")
        elif "not authorized" in error_msg.lower():
            print(f"Error de autorización: El usuario no tiene permisos para crear índices")
        else:
            print(f"Error creando índices: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"Error inesperado: {type(e).__name__}: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
    finally:
        if 'client' in locals():
            client.close()
            print("Conexión cerrada")


if __name__ == "__main__":
    print("Inicializando índices MongoDB...\n")
    asyncio.run(init_indexes())


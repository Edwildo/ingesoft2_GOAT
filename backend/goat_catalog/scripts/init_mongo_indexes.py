"""Script para inicializar índices TTL en MongoDB."""

import asyncio
import sys
from pathlib import Path

# Agregar src al path
sys.path.insert(0, str(Path(__file__).parent.parent / "src"))

from motor.motor_asyncio import AsyncIOMotorClient
from pymongo.errors import OperationFailure
from pymongo.server_api import ServerApi

from goat_catalog.shared.config import get_settings
from goat_catalog.shared.infrastructure.mongo_indexes import initialize_otp_indexes


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

        success = await initialize_otp_indexes(database)
        
        if success:
            print("\n✓ Todos los índices fueron inicializados correctamente")
        else:
            print("\n⚠ Algunos índices no pudieron ser creados. Revisa los errores arriba.")
            sys.exit(1)

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


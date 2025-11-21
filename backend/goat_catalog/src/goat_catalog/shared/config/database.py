"""Configuración de conexión a MongoDB."""

from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase
from pymongo.errors import ConnectionFailure
from pymongo.server_api import ServerApi

from .settings import get_settings


class Database:
    """Clase para gestionar la conexión a MongoDB."""

    client: AsyncIOMotorClient | None = None
    database: AsyncIOMotorDatabase | None = None

    @classmethod
    async def connect(cls) -> None:
        """Establece conexión con MongoDB (local o Atlas)."""
        import logging
        
        logger = logging.getLogger(__name__)
        settings = get_settings()

        try:
            # Determinar si es una conexión Atlas (mongodb+srv://)
            is_atlas = settings.mongodb_uri.startswith("mongodb+srv://")

            # Configurar cliente con ServerApi si es Atlas
            if is_atlas:
                logger.info("Conectando a MongoDB Atlas...")
                cls.client = AsyncIOMotorClient(
                    settings.mongodb_uri,
                    server_api=ServerApi("1"),
                    serverSelectionTimeoutMS=5000,  # Timeout más corto para no bloquear
                )
            else:
                logger.info("Conectando a MongoDB local...")
                cls.client = AsyncIOMotorClient(
                    settings.mongodb_uri,
                    serverSelectionTimeoutMS=5000,
                )

            cls.database = cls.client[settings.mongodb_database]

            # Verificar conexión con timeout corto
            await cls.client.admin.command("ping")
            logger.info("✅ Conexión a MongoDB establecida correctamente")
            
        except Exception as e:
            logger.error(f"⚠️  Error conectando a MongoDB: {e}")
            logger.warning("El servidor continuará, pero la conexión a MongoDB fallará hasta que se corrija")
            # No lanzamos la excepción para que el servidor pueda iniciar
            # La conexión se reintentará cuando se use el repositorio

    @classmethod
    async def disconnect(cls) -> None:
        """Cierra la conexión con MongoDB."""
        if cls.client:
            cls.client.close()
            cls.client = None
            cls.database = None

    @classmethod
    def get_database(cls) -> AsyncIOMotorDatabase:
        """Obtiene la instancia de la base de datos."""
        if cls.database is None:
            raise RuntimeError("Database no está conectada. Llama a connect() primero.")
        return cls.database


def get_database() -> AsyncIOMotorDatabase:
    """Obtiene la instancia de la base de datos MongoDB."""
    return Database.get_database()


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

            if is_atlas:
                logger.info("Conectando a MongoDB Atlas...")
                cls.client = AsyncIOMotorClient(
                    settings.mongodb_uri,
                    server_api=ServerApi("1"),
                    serverSelectionTimeoutMS=5000,
                )
            else:
                logger.info("Conectando a MongoDB local...")
                cls.client = AsyncIOMotorClient(
                    settings.mongodb_uri,
                    serverSelectionTimeoutMS=5000,
                )

            cls.database = cls.client[settings.mongodb_database]

            await cls.client.admin.command("ping")
            logger.info("Conexion a MongoDB establecida correctamente")
            
        except Exception as e:
            logger.error(f"Error conectando a MongoDB: {type(e).__name__}")
            logger.warning("El servidor continuara, pero la conexion a MongoDB fallara hasta que se corrija")

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


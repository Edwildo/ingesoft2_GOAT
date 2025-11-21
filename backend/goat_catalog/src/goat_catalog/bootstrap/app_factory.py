"""Factory para crear y configurar la aplicación FastAPI."""

import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from ..shared.config import get_settings
from ..shared.config.database import Database

# Configurar logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)


def create_app() -> FastAPI:
    """Crea y configura la aplicación FastAPI.

    Returns:
        Instancia configurada de FastAPI
    """
    settings = get_settings()

    app = FastAPI(
        title="GOAT Catalog & Authentication Service",
        description="Microservicio para catálogo canónico y tokens de autenticación",
        version="1.0.0",
        docs_url="/docs",
        redoc_url="/redoc",
    )

    # CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origins_list,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # Event handlers
    @app.on_event("startup")
    async def startup_event() -> None:
        """Evento de inicio: conecta a MongoDB."""
        import logging
        logger = logging.getLogger(__name__)
        try:
            await Database.connect()
        except Exception as e:
            logger.error(f"Error en startup: {e}. El servidor continuará funcionando.")

    @app.on_event("shutdown")
    async def shutdown_event() -> None:
        """Evento de cierre: desconecta de MongoDB."""
        try:
            await Database.disconnect()
        except Exception:
            pass  # Ignorar errores al desconectar

    return app


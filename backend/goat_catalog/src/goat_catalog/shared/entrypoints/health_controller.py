"""Controller para health check."""

import sys
from pathlib import Path

from fastapi import APIRouter, status
from pydantic import BaseModel

sys.path.insert(0, str(Path(__file__).parent.parent.parent))

from goat_catalog.shared.config.database import get_database
from goat_catalog.shared.infrastructure.mongo_indexes import verify_otp_indexes

router = APIRouter(tags=["Health"])


class HealthResponse(BaseModel):
    """Response del health check."""

    status: str = "ok"
    service: str = "goat-catalog-service"
    version: str = "1.0.0"
    database: str = "connected"
    indexes: dict[str, bool] = {}


@router.get("/health", response_model=HealthResponse, status_code=status.HTTP_200_OK)
async def health_check() -> HealthResponse:
    """Endpoint de health check para verificar que el servicio está funcionando.

    Verifica:
    - Estado del servicio
    - Conexión a MongoDB
    - Existencia de índices requeridos

    Returns:
        Response con el estado del servicio y componentes
    """
    try:
        database = get_database()
        indexes_status = await verify_otp_indexes(database)
        all_indexes_ok = all(indexes_status.values())
        
        return HealthResponse(
            status="ok" if all_indexes_ok else "degraded",
            database="connected",
            indexes=indexes_status,
        )
    except Exception:
        return HealthResponse(
            status="unhealthy",
            database="disconnected",
            indexes={},
        )


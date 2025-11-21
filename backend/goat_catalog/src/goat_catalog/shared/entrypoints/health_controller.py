"""Controller para health check."""

from fastapi import APIRouter, status
from pydantic import BaseModel

router = APIRouter(tags=["Health"])


class HealthResponse(BaseModel):
    """Response del health check."""

    status: str = "ok"
    service: str = "goat-catalog-service"
    version: str = "1.0.0"


@router.get("/health", response_model=HealthResponse, status_code=status.HTTP_200_OK)
async def health_check() -> HealthResponse:
    """Endpoint de health check para verificar que el servicio está funcionando.

    Returns:
        Response con el estado del servicio
    """
    return HealthResponse()


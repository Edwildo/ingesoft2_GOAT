"""Controller REST para operaciones con Collections."""

import sys
from pathlib import Path
from typing import List

from fastapi import APIRouter, HTTPException, status

from ...application.dto.collection_response import CollectionResponse
from ...application.use_cases.get_all_collections_use_case import GetAllCollectionsUseCase
from ...domain.repositories.collection_repository import CollectionRepository
from ...infrastructure.persistence.mongo_collection_repository import MongoCollectionRepository

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

router = APIRouter(prefix="/api/catalog", tags=["Catalog"])


def _get_collection_repository() -> CollectionRepository:
    """Factory para obtener instancia de CollectionRepository."""
    return MongoCollectionRepository()


def _get_get_all_collections_use_case() -> GetAllCollectionsUseCase:
    """Factory para obtener instancia de GetAllCollectionsUseCase."""
    collection_repository: CollectionRepository = _get_collection_repository()
    return GetAllCollectionsUseCase(collection_repository)


@router.get(
    "/collections",
    response_model=dict,
    status_code=status.HTTP_200_OK,
)
async def get_all_collections() -> dict:
    """Obtiene todas las colecciones del catálogo.
    
    Returns:
        Lista de todas las colecciones
    """
    try:
        use_case = _get_get_all_collections_use_case()
        collections = await use_case.execute()
        
        return {
            "collections": collections,
        }
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al obtener colecciones",
        ) from e


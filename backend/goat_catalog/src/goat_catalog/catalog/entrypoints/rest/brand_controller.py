"""Controller REST para operaciones con Brands."""

import sys
from pathlib import Path
from typing import List

from fastapi import APIRouter, HTTPException, status

from ...application.dto.brand_response import BrandResponse
from ...application.use_cases.get_all_brands_use_case import GetAllBrandsUseCase
from ...domain.repositories.brand_repository import BrandRepository
from ...infrastructure.persistence.mongo_brand_repository import MongoBrandRepository

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

router = APIRouter(prefix="/api/catalog", tags=["Catalog"])


def _get_brand_repository() -> BrandRepository:
    """Factory para obtener instancia de BrandRepository."""
    return MongoBrandRepository()


def _get_get_all_brands_use_case() -> GetAllBrandsUseCase:
    """Factory para obtener instancia de GetAllBrandsUseCase."""
    brand_repository: BrandRepository = _get_brand_repository()
    return GetAllBrandsUseCase(brand_repository)


@router.get(
    "/brands",
    response_model=dict,
    status_code=status.HTTP_200_OK,
)
async def get_all_brands() -> dict:
    """Obtiene todas las marcas del catálogo.
    
    Returns:
        Lista de todas las marcas
    """
    try:
        use_case = _get_get_all_brands_use_case()
        brands = await use_case.execute()
        
        return {
            "brands": brands,
        }
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al obtener marcas",
        ) from e


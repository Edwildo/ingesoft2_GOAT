"""Controller REST para operaciones con Categories."""

import sys
from pathlib import Path
from typing import List

from fastapi import APIRouter, HTTPException, status

from ...application.dto.category_response import CategoryResponse
from ...application.use_cases.get_all_categories_use_case import GetAllCategoriesUseCase
from ...domain.repositories.category_repository import CategoryRepository
from ...infrastructure.persistence.mongo_category_repository import MongoCategoryRepository

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

router = APIRouter(prefix="/api/catalog", tags=["Catalog"])


def _get_category_repository() -> CategoryRepository:
    """Factory para obtener instancia de CategoryRepository."""
    return MongoCategoryRepository()


def _get_get_all_categories_use_case() -> GetAllCategoriesUseCase:
    """Factory para obtener instancia de GetAllCategoriesUseCase."""
    category_repository: CategoryRepository = _get_category_repository()
    return GetAllCategoriesUseCase(category_repository)


@router.get(
    "/categories",
    response_model=dict,
    status_code=status.HTTP_200_OK,
)
async def get_all_categories() -> dict:
    """Obtiene todas las categorías del catálogo.
    
    Returns:
        Lista de todas las categorías
    """
    try:
        use_case = _get_get_all_categories_use_case()
        categories = await use_case.execute()
        
        return {
            "categories": categories,
        }
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al obtener categorías",
        ) from e


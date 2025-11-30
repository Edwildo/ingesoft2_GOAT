"""Controller REST para operaciones con Sneakers."""

import sys
from pathlib import Path
from typing import List

from fastapi import APIRouter, HTTPException, Query, status

from ...application.dto.search_filters import SearchFilters
from ...application.dto.sneaker_request import SneakerRequest
from ...application.dto.sneaker_response import SneakerResponse
from ...application.use_cases.create_sneaker_use_case import CreateSneakerUseCase
from ...application.use_cases.get_sneaker_by_sku_use_case import GetSneakerBySkuUseCase
from ...application.use_cases.search_sneakers_use_case import SearchSneakersUseCase
from ...domain.exceptions.duplicate_sku_exception import DuplicateSkuException
from ...domain.exceptions.sneaker_not_found_exception import SneakerNotFoundException
from ...domain.repositories.brand_repository import BrandRepository
from ...domain.repositories.category_repository import CategoryRepository
from ...domain.repositories.collection_repository import CollectionRepository
from ...domain.repositories.sneaker_repository import SneakerRepository
from ...infrastructure.persistence.mongo_brand_repository import MongoBrandRepository
from ...infrastructure.persistence.mongo_category_repository import MongoCategoryRepository
from ...infrastructure.persistence.mongo_collection_repository import MongoCollectionRepository
from ...infrastructure.persistence.mongo_sneaker_repository import MongoSneakerRepository

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

router = APIRouter(prefix="/api/catalog", tags=["Catalog"])


def _get_sneaker_repository() -> SneakerRepository:
    """Factory para obtener instancia de SneakerRepository."""
    return MongoSneakerRepository()


def _get_get_sneaker_by_sku_use_case() -> GetSneakerBySkuUseCase:
    """Factory para obtener instancia de GetSneakerBySkuUseCase."""
    sneaker_repository: SneakerRepository = _get_sneaker_repository()
    return GetSneakerBySkuUseCase(sneaker_repository)


def _get_search_sneakers_use_case() -> SearchSneakersUseCase:
    """Factory para obtener instancia de SearchSneakersUseCase."""
    sneaker_repository: SneakerRepository = _get_sneaker_repository()
    return SearchSneakersUseCase(sneaker_repository)


def _get_create_sneaker_use_case() -> CreateSneakerUseCase:
    """Factory para obtener instancia de CreateSneakerUseCase."""
    sneaker_repository: SneakerRepository = _get_sneaker_repository()
    brand_repository: BrandRepository = MongoBrandRepository()
    category_repository: CategoryRepository = MongoCategoryRepository()
    collection_repository: CollectionRepository = MongoCollectionRepository()
    return CreateSneakerUseCase(
        sneaker_repository=sneaker_repository,
        brand_repository=brand_repository,
        category_repository=category_repository,
        collection_repository=collection_repository,
    )


@router.get(
    "/sneakers/{sku}",
    response_model=SneakerResponse,
    status_code=status.HTTP_200_OK,
)
async def get_sneaker_by_sku(sku: str) -> SneakerResponse:
    """Obtiene un sneaker por su SKU.
    
    Este endpoint es CRÍTICO para la validación de SKUs desde el backend Java.
    
    Args:
        sku: SKU único del sneaker
        
    Returns:
        Sneaker encontrado
        
    Raises:
        HTTPException: 404 si el sneaker no existe
    """
    try:
        use_case = _get_get_sneaker_by_sku_use_case()
        return await use_case.execute(sku)
    except SneakerNotFoundException as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(e),
        ) from e
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        ) from e
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al buscar sneaker",
        ) from e


@router.get(
    "/sneakers",
    response_model=dict,
    status_code=status.HTTP_200_OK,
)
async def search_sneakers(
    brand: str | None = Query(None, description="Filtrar por marca"),
    category: str | None = Query(None, description="Filtrar por categoría"),
    gender: str | None = Query(None, description="Filtrar por género (MALE, FEMALE, UNISEX)"),
    collection: str | None = Query(None, description="Filtrar por colección"),
    search: str | None = Query(None, description="Texto de búsqueda"),
    page: int = Query(1, ge=1, description="Número de página"),
    size: int = Query(20, ge=1, le=100, description="Tamaño de página"),
) -> dict:
    """Busca sneakers con filtros opcionales.
    
    Args:
        brand: Filtrar por marca (ID o nombre)
        category: Filtrar por categoría (ID o slug)
        gender: Filtrar por género
        collection: Filtrar por colección (ID o slug)
        search: Texto de búsqueda (busca en modelo y descripción)
        page: Número de página (empezando en 1)
        size: Tamaño de página (máximo 100)
        
    Returns:
        Respuesta con lista de sneakers, total y paginación
    """
    try:
        filters = SearchFilters(
            brand=brand,
            category=category,
            gender=gender,
            collection=collection,
            search=search,
            page=page,
            size=size,
        )
        
        use_case = _get_search_sneakers_use_case()
        sneakers, total = await use_case.execute(filters)
        
        return {
            "sneakers": sneakers,
            "total": total,
            "page": page,
            "size": size,
        }
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al buscar sneakers",
        ) from e


@router.post(
    "/sneakers",
    response_model=SneakerResponse,
    status_code=status.HTTP_201_CREATED,
)
async def create_sneaker(body: SneakerRequest) -> SneakerResponse:
    """Crea un nuevo sneaker en el catálogo.
    
    Este endpoint permite crear sneakers que luego pueden ser usados para crear listings.
    El SKU debe ser único. Si ya existe, se retornará un error.
    
    Args:
        body: Datos del sneaker a crear
        
    Returns:
        Sneaker creado
        
    Raises:
        HTTPException: 409 si el SKU ya existe, 400 si hay datos inválidos
    """
    try:
        use_case = _get_create_sneaker_use_case()
        return await use_case.execute(body)
    except DuplicateSkuException as e:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(e),
        ) from e
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        ) from e
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno al crear sneaker",
        ) from e


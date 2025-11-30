"""REST Controllers para el catálogo."""

from .brand_controller import router as brand_router
from .category_controller import router as category_router
from .collection_controller import router as collection_router
from .sneaker_controller import router as sneaker_router

__all__ = [
    "sneaker_router",
    "brand_router",
    "category_router",
    "collection_router",
]


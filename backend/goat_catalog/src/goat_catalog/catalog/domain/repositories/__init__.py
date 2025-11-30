"""Repositorios (puertos) del dominio del catálogo."""

from .brand_repository import BrandRepository
from .category_repository import CategoryRepository
from .collection_repository import CollectionRepository
from .sneaker_repository import SneakerRepository

__all__ = [
    "SneakerRepository",
    "BrandRepository",
    "CategoryRepository",
    "CollectionRepository",
]


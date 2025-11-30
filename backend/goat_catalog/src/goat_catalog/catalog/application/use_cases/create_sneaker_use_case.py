"""Caso de uso para crear un nuevo sneaker."""

from ...domain.entities.sneaker import Sneaker
from ...domain.exceptions.duplicate_sku_exception import DuplicateSkuException
from ...domain.repositories.brand_repository import BrandRepository
from ...domain.repositories.category_repository import CategoryRepository
from ...domain.repositories.collection_repository import CollectionRepository
from ...domain.repositories.sneaker_repository import SneakerRepository
from ...domain.value_objects.media import Media
from ...domain.value_objects.sku import SKU
from ..dto.sneaker_request import MediaRequest, SneakerRequest
from ..dto.sneaker_response import MediaResponse, SneakerResponse


class CreateSneakerUseCase:
    """Caso de uso para crear un nuevo sneaker en el catálogo."""

    def __init__(
        self,
        sneaker_repository: SneakerRepository,
        brand_repository: BrandRepository | None = None,
        category_repository: CategoryRepository | None = None,
        collection_repository: CollectionRepository | None = None,
    ) -> None:
        """Inicializa el caso de uso.

        Args:
            sneaker_repository: Repositorio para guardar sneakers
            brand_repository: Repositorio para validar marcas (opcional)
            category_repository: Repositorio para validar categorías (opcional)
            collection_repository: Repositorio para validar colecciones (opcional)
        """
        self._sneaker_repository = sneaker_repository
        self._brand_repository = brand_repository
        self._category_repository = category_repository
        self._collection_repository = collection_repository

    async def execute(self, request: SneakerRequest) -> SneakerResponse:
        """Ejecuta la creación de un nuevo sneaker.

        Args:
            request: DTO con los datos del sneaker a crear

        Returns:
            SneakerResponse con el sneaker creado

        Raises:
            DuplicateSkuException: Si el SKU ya existe
            ValueError: Si los datos son inválidos
        """
        # Validar y crear SKU
        sku = SKU(request.sku)

        # Verificar si el SKU ya existe
        existing_sneaker = await self._sneaker_repository.find_by_sku(sku)
        if existing_sneaker:
            raise DuplicateSkuException(request.sku)

        # Validar brand si se proporciona y el repositorio está disponible
        if self._brand_repository and request.brand:
            brand = await self._brand_repository.find_by_id(request.brand)
            if not brand:
                brand = await self._brand_repository.find_by_slug(request.brand)
            # Si no se encuentra la marca, continuamos (asumimos que es válida o se creará después)

        # Validar categorías si se proporcionan
        if self._category_repository and request.categories:
            for category_id in request.categories:
                category = await self._category_repository.find_by_id(category_id)
                if not category:
                    category = await self._category_repository.find_by_slug(category_id)
                # Si no se encuentra, continuamos (asumimos que es válida)

        # Validar colecciones si se proporcionan
        if self._collection_repository and request.collections:
            for collection_id in request.collections:
                collection = await self._collection_repository.find_by_id(collection_id)
                if not collection:
                    # Si no se encuentra, continuamos (asumimos que es válida)
                    pass

        # Crear Media value object
        media = Media()
        if request.media:
            media = Media(
                cover_image=request.media.cover_image,
                gallery=request.media.gallery or [],
            )

        # Crear entidad de dominio
        sneaker = Sneaker(
            sku=sku,
            brand=request.brand,
            model=request.model,
            gender=request.gender,
            description=request.description,
            categories=request.categories or [],
            collections=request.collections or [],
            media=media,
        )

        # Guardar en repositorio
        await self._sneaker_repository.save(sneaker)

        # Convertir a DTO de respuesta
        return self._to_response(sneaker)

    def _to_response(self, sneaker: Sneaker) -> SneakerResponse:
        """Convierte una entidad Sneaker a SneakerResponse.

        Args:
            sneaker: Entidad de dominio

        Returns:
            DTO de respuesta
        """
        media = sneaker.media
        media_response = MediaResponse(
            cover_image=media.cover_image,
            gallery=media.gallery,
        )

        return SneakerResponse(
            sku=sneaker.sku.value,
            brand=sneaker.brand,
            model=sneaker.model,
            gender=sneaker.gender,
            description=sneaker.description,
            categories=sneaker.categories,
            collections=sneaker.collections,
            media=media_response,
        )


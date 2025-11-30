"""Value Object Media - Representa información de medios de un producto."""

from typing import List, Optional
from typing_extensions import Self


class Media:
    """Value Object inmutable que representa medios de un producto.
    
    Incluye imagen de portada y galería de imágenes.
    """

    def __init__(
        self,
        cover_image: Optional[str] = None,
        gallery: Optional[List[str]] = None,
    ) -> None:
        """Inicializa un Media.

        Args:
            cover_image: URL de la imagen de portada
            gallery: Lista de URLs de imágenes adicionales

        Raises:
            ValueError: Si algún URL no es válido
        """
        self._cover_image = self._validate_url(cover_image) if cover_image else None
        self._gallery = self._validate_gallery(gallery) if gallery else []

    @classmethod
    def _validate_url(cls, url: Optional[str]) -> Optional[str]:
        """Valida que un URL sea válido."""
        if url is None:
            return None

        if not isinstance(url, str):
            raise ValueError(f"URL debe ser un string: {type(url)}")

        url = url.strip()

        if not url:
            return None

        # Validación básica: debe empezar con http:// o https://
        if not (url.startswith("http://") or url.startswith("https://")):
            raise ValueError(f"URL debe empezar con http:// o https://: {url}")

        return url

    @classmethod
    def _validate_gallery(cls, gallery: Optional[List[str]]) -> List[str]:
        """Valida una lista de URLs de galería."""
        if gallery is None:
            return []

        if not isinstance(gallery, list):
            raise ValueError(f"Galería debe ser una lista: {type(gallery)}")

        validated_urls = []
        for url in gallery:
            validated_url = cls._validate_url(url)
            if validated_url:
                validated_urls.append(validated_url)

        return validated_urls

    @property
    def cover_image(self) -> Optional[str]:
        """Retorna la URL de la imagen de portada."""
        return self._cover_image

    @property
    def gallery(self) -> List[str]:
        """Retorna la lista de URLs de la galería."""
        return self._gallery.copy()

    def has_cover_image(self) -> bool:
        """Verifica si tiene imagen de portada."""
        return self._cover_image is not None

    def has_gallery(self) -> bool:
        """Verifica si tiene imágenes en la galería."""
        return len(self._gallery) > 0

    def __eq__(self, other: object) -> bool:
        """Compara dos Media por igualdad."""
        if not isinstance(other, Media):
            return False
        return (
            self._cover_image == other._cover_image
            and self._gallery == other._gallery
        )

    def __hash__(self) -> int:
        """Retorna el hash del Media."""
        return hash((self._cover_image, tuple(self._gallery)))

    def __repr__(self) -> str:
        """Representación del Media."""
        return f"Media(cover_image={self._cover_image}, gallery={len(self._gallery)} images)"


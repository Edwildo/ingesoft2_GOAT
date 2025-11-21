"""Documento MongoDB para emails confirmados."""

from datetime import datetime

from pydantic import BaseModel, Field


class ConfirmedEmailDocument(BaseModel):
    """Documento MongoDB que representa un email confirmado."""

    email: str = Field(..., description="Email del usuario")
    confirmed_at: datetime = Field(..., description="Fecha de confirmación")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "email": "user@example.com",
                    "confirmed_at": "2024-01-01T00:00:00",
                }
            ]
        }
    }

    @classmethod
    def from_entity(cls, confirmed_email) -> "ConfirmedEmailDocument":
        """Crea un documento desde una entidad de dominio."""
        from ...domain.entities.confirmed_email import ConfirmedEmail

        if not isinstance(confirmed_email, ConfirmedEmail):
            raise ValueError("Debe ser una instancia de ConfirmedEmail")

        return cls(
            email=confirmed_email.email.value,
            confirmed_at=confirmed_email.confirmed_at,
        )

    def to_entity(self):
        """Convierte el documento a una entidad de dominio."""
        from ...domain.entities.confirmed_email import ConfirmedEmail
        from ...domain.value_objects.email import Email

        return ConfirmedEmail(
            email=Email(self.email),
            confirmed_at=self.confirmed_at,
        )


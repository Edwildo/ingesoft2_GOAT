"""Documento MongoDB para OTPs."""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class OTPDocument(BaseModel):
    """Documento MongoDB que representa un OTP."""

    email: str = Field(..., description="Email del usuario")
    purpose: str = Field(..., description="Propósito del OTP")
    otp_hash: str = Field(..., description="Hash del código OTP")
    attempts: int = Field(default=0, description="Número de intentos de validación")
    created_at: datetime = Field(..., description="Fecha de creación")
    expire_at: datetime = Field(..., description="Fecha de expiración (TTL)")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "email": "user@example.com",
                    "purpose": "REGISTER",
                    "otp_hash": "abc123...",
                    "attempts": 0,
                    "created_at": "2024-01-01T00:00:00",
                    "expire_at": "2024-01-01T00:05:00",
                }
            ]
        }
    }

    @classmethod
    def from_entity(cls, otp_token) -> "OTPDocument":
        """Crea un documento desde una entidad de dominio."""
        from ...domain.entities.otp_token import OTPToken

        if not isinstance(otp_token, OTPToken):
            raise ValueError("Debe ser una instancia de OTPToken")

        return cls(
            email=otp_token.email.value,
            purpose=otp_token.purpose.value,
            otp_hash=otp_token.otp_hash.value,
            attempts=otp_token.attempts,
            created_at=otp_token.created_at,
            expire_at=otp_token.expire_at,
        )

    def to_entity(self):
        """Convierte el documento a una entidad de dominio."""
        from ...domain.entities.otp_token import OTPToken
        from ...domain.value_objects.email import Email
        from ...domain.value_objects.otp_hash import OTPHash
        from ...domain.value_objects.otp_purpose import OTPPurpose

        return OTPToken(
            email=Email(self.email),
            purpose=OTPPurpose(self.purpose),
            otp_hash=OTPHash(self.otp_hash),
            created_at=self.created_at,
            expire_at=self.expire_at,
            attempts=self.attempts,
        )


"""DTO para respuesta de verificación de estado de email."""

from pydantic import BaseModel, Field


class VerifyEmailStatusResponse(BaseModel):
    """Response para verificar el estado de confirmación de un email."""

    confirmed: bool = Field(..., description="Indica si el email está confirmado")
    email: str = Field(..., description="Email del usuario")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "confirmed": True,
                    "email": "user@example.com",
                }
            ]
        }
    }


"""DTO para respuesta de confirmación de email."""

from pydantic import BaseModel, Field


class ConfirmEmailResponse(BaseModel):
    """Response para confirmar un email."""

    email: str = Field(..., description="Email del usuario")
    confirmed: bool = Field(..., description="Indica si el email está confirmado")
    message: str = Field(..., description="Mensaje descriptivo")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "email": "user@example.com",
                    "confirmed": True,
                    "message": "Email confirmado exitosamente",
                }
            ]
        }
    }

"""DTO para respuesta de generación de OTP."""

from pydantic import BaseModel, Field


class GenerateOTPResponse(BaseModel):
    """Response después de generar un OTP."""

    success: bool = Field(..., description="Indica si se generó correctamente")
    message: str = Field(..., description="Mensaje descriptivo")
    expires_in_minutes: int = Field(..., description="Minutos hasta la expiración")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "success": True,
                    "message": "OTP generado y enviado correctamente",
                    "expires_in_minutes": 5,
                }
            ]
        }
    }


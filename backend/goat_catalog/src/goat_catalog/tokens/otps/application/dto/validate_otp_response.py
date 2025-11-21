"""DTO para respuesta de validación de OTP."""

from pydantic import BaseModel, Field


class ValidateOTPResponse(BaseModel):
    """Response después de validar un OTP."""

    success: bool = Field(..., description="Indica si la validación fue exitosa")
    message: str = Field(..., description="Mensaje descriptivo")
    valid: bool = Field(..., description="Indica si el OTP es válido")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "success": True,
                    "message": "OTP válido",
                    "valid": True,
                }
            ]
        }
    }


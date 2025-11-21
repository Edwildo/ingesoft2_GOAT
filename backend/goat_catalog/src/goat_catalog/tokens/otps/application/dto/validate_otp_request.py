"""DTO para solicitud de validación de OTP."""

from pydantic import BaseModel, Field

from ...domain.value_objects.otp_purpose import OTPPurpose


class ValidateOTPRequest(BaseModel):
    """Request para validar un OTP."""

    email: str = Field(..., description="Email del usuario", examples=["user@example.com"])
    otp: str = Field(..., description="Código OTP a validar", examples=["123456"])
    purpose: OTPPurpose = Field(
        ..., description="Propósito del OTP", examples=[OTPPurpose.REGISTER]
    )

    model_config = {
        "json_schema_extra": {
            "examples": [
                {"email": "user@example.com", "otp": "123456", "purpose": "REGISTER"}
            ]
        }
    }


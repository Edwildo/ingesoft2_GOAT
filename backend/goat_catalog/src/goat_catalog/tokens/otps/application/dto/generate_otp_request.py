"""DTO para solicitud de generación de OTP."""

from pydantic import BaseModel, Field

from ...domain.value_objects.otp_purpose import OTPPurpose


class GenerateOTPRequest(BaseModel):
    """Request para generar un nuevo OTP."""

    email: str = Field(..., description="Email del usuario", examples=["user@example.com"])
    purpose: OTPPurpose = Field(
        ..., description="Propósito del OTP", examples=[OTPPurpose.REGISTER]
    )

    model_config = {"json_schema_extra": {"examples": [{"email": "user@example.com", "purpose": "REGISTER"}]}}


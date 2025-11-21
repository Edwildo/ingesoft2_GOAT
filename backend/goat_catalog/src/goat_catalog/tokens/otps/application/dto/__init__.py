"""DTOs del agregado OTPs."""

from .confirm_email_response import ConfirmEmailResponse
from .generate_otp_request import GenerateOTPRequest
from .generate_otp_response import GenerateOTPResponse
from .validate_otp_request import ValidateOTPRequest
from .validate_otp_response import ValidateOTPResponse
from .verify_email_status_response import VerifyEmailStatusResponse

__all__ = [
    "ConfirmEmailResponse",
    "GenerateOTPRequest",
    "GenerateOTPResponse",
    "ValidateOTPRequest",
    "ValidateOTPResponse",
    "VerifyEmailStatusResponse",
]


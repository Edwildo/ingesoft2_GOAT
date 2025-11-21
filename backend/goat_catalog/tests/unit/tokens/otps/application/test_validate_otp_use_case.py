"""Tests unitarios para el caso de uso ValidateOTPUseCase."""

from datetime import datetime, timedelta
from unittest.mock import AsyncMock

import pytest

from goat_catalog.tokens.otps.application.dto.validate_otp_request import (
    ValidateOTPRequest,
)
from goat_catalog.tokens.otps.application.use_cases.validate_otp_use_case import (
    ValidateOTPUseCase,
)
from goat_catalog.tokens.otps.domain.entities.otp_token import OTPToken
from goat_catalog.tokens.otps.domain.exceptions.otp_expired_exception import (
    OTPExpiredException,
)
from goat_catalog.tokens.otps.domain.exceptions.otp_invalid_exception import (
    OTPInvalidException,
)
from goat_catalog.tokens.otps.domain.value_objects.email import Email
from goat_catalog.tokens.otps.domain.value_objects.otp_hash import OTPHash
from goat_catalog.tokens.otps.domain.value_objects.otp_purpose import OTPPurpose
from goat_catalog.shared.utils.security import hash_otp


class TestValidateOTPUseCase:
    """Tests para el caso de uso ValidateOTPUseCase."""

    @pytest.mark.asyncio
    async def test_validate_otp_success(self) -> None:
        """Test que verifica la validación exitosa de un OTP."""
        # Arrange
        otp_code = "123456"
        email = Email("test@example.com")
        purpose = OTPPurpose.REGISTER
        otp_hash = OTPHash(hash_otp(otp_code))
        now = datetime.utcnow()
        expire_at = now + timedelta(minutes=5)

        otp_token = OTPToken(
            email=email,
            purpose=purpose,
            otp_hash=otp_hash,
            created_at=now,
            expire_at=expire_at,
            attempts=0,
        )

        otp_repository = AsyncMock()
        otp_repository.find_by_email_and_purpose = AsyncMock(return_value=otp_token)
        otp_repository.save = AsyncMock()
        otp_repository.delete = AsyncMock()

        use_case = ValidateOTPUseCase(otp_repository)

        request = ValidateOTPRequest(
            email="test@example.com",
            otp=otp_code,
            purpose=OTPPurpose.REGISTER,
        )

        # Act
        response = await use_case.execute(request)

        # Assert
        assert response.success is True
        assert response.valid is True
        otp_repository.find_by_email_and_purpose.assert_called_once()
        otp_repository.delete.assert_called_once()

    @pytest.mark.asyncio
    async def test_validate_otp_invalid_raises_exception(self) -> None:
        """Test que verifica que un OTP inválido lanza excepción."""
        # Arrange
        otp_code = "123456"
        invalid_code = "999999"
        email = Email("test@example.com")
        purpose = OTPPurpose.REGISTER
        otp_hash = OTPHash(hash_otp(otp_code))
        now = datetime.utcnow()
        expire_at = now + timedelta(minutes=5)

        otp_token = OTPToken(
            email=email,
            purpose=purpose,
            otp_hash=otp_hash,
            created_at=now,
            expire_at=expire_at,
            attempts=0,
        )

        otp_repository = AsyncMock()
        otp_repository.find_by_email_and_purpose = AsyncMock(return_value=otp_token)
        otp_repository.save = AsyncMock()

        use_case = ValidateOTPUseCase(otp_repository)

        request = ValidateOTPRequest(
            email="test@example.com",
            otp=invalid_code,
            purpose=OTPPurpose.REGISTER,
        )

        # Act & Assert
        with pytest.raises(OTPInvalidException, match="OTP inválido"):
            await use_case.execute(request)

    @pytest.mark.asyncio
    async def test_validate_otp_not_found_raises_exception(self) -> None:
        """Test que verifica que un OTP no encontrado lanza excepción."""
        # Arrange
        otp_repository = AsyncMock()
        otp_repository.find_by_email_and_purpose = AsyncMock(return_value=None)

        use_case = ValidateOTPUseCase(otp_repository)

        request = ValidateOTPRequest(
            email="test@example.com",
            otp="123456",
            purpose=OTPPurpose.REGISTER,
        )

        # Act & Assert
        with pytest.raises(OTPInvalidException, match="OTP no encontrado"):
            await use_case.execute(request)

    @pytest.mark.asyncio
    async def test_validate_otp_expired_raises_exception(self) -> None:
        """Test que verifica que un OTP expirado lanza excepción."""
        # Arrange
        otp_code = "123456"
        email = Email("test@example.com")
        purpose = OTPPurpose.REGISTER
        otp_hash = OTPHash(hash_otp(otp_code))
        now = datetime.utcnow()
        expire_at = now - timedelta(minutes=1)

        otp_token = OTPToken(
            email=email,
            purpose=purpose,
            otp_hash=otp_hash,
            created_at=now,
            expire_at=expire_at,
            attempts=0,
        )

        otp_repository = AsyncMock()
        otp_repository.find_by_email_and_purpose = AsyncMock(return_value=otp_token)
        otp_repository.save = AsyncMock()
        otp_repository.delete = AsyncMock()

        use_case = ValidateOTPUseCase(otp_repository)

        request = ValidateOTPRequest(
            email="test@example.com",
            otp=otp_code,
            purpose=OTPPurpose.REGISTER,
        )

        # Act & Assert
        with pytest.raises(OTPExpiredException, match="OTP ha expirado"):
            await use_case.execute(request)


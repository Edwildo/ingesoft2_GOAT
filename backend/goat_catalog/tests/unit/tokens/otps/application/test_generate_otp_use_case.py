"""Tests unitarios para el caso de uso GenerateOTPUseCase."""

from unittest.mock import AsyncMock, MagicMock

import pytest

from goat_catalog.tokens.otps.application.dto.generate_otp_request import (
    GenerateOTPRequest,
)
from goat_catalog.tokens.otps.application.use_cases.generate_otp_use_case import (
    GenerateOTPUseCase,
)
from goat_catalog.tokens.otps.domain.value_objects.otp_purpose import OTPPurpose


class TestGenerateOTPUseCase:
    """Tests para el caso de uso GenerateOTPUseCase."""

    @pytest.mark.asyncio
    async def test_generate_otp_success(self) -> None:
        """Test que verifica la generación exitosa de un OTP."""
        # Arrange
        otp_repository = AsyncMock()
        otp_repository.delete_by_email_and_purpose = AsyncMock()
        otp_repository.save = AsyncMock()

        otp_generator_service = MagicMock()
        otp_generator_service.generate.return_value = "123456"

        use_case = GenerateOTPUseCase(otp_repository, otp_generator_service)

        request = GenerateOTPRequest(
            email="test@example.com",
            purpose=OTPPurpose.REGISTER,
        )

        # Act
        response = await use_case.execute(request)

        # Assert
        assert response.success is True
        assert response.expires_in_minutes == 5
        otp_repository.delete_by_email_and_purpose.assert_called_once()
        otp_repository.save.assert_called_once()
        otp_generator_service.generate.assert_called_once()

    @pytest.mark.asyncio
    async def test_generate_otp_deletes_existing_otp(self) -> None:
        """Test que verifica que se elimina un OTP existente antes de crear uno nuevo."""
        # Arrange
        otp_repository = AsyncMock()
        otp_repository.delete_by_email_and_purpose = AsyncMock()
        otp_repository.save = AsyncMock()

        otp_generator_service = MagicMock()
        otp_generator_service.generate.return_value = "654321"

        use_case = GenerateOTPUseCase(otp_repository, otp_generator_service)

        request = GenerateOTPRequest(
            email="test@example.com",
            purpose=OTPPurpose.LOGIN,
        )

        # Act
        await use_case.execute(request)

        # Assert
        otp_repository.delete_by_email_and_purpose.assert_called_once()
        otp_repository.save.assert_called_once()


"""Tests unitarios para la entidad OTPToken."""

from datetime import datetime, timedelta

import pytest

from goat_catalog.tokens.otps.domain.entities.otp_token import OTPToken
from goat_catalog.tokens.otps.domain.value_objects.email import Email
from goat_catalog.tokens.otps.domain.value_objects.otp_hash import OTPHash
from goat_catalog.tokens.otps.domain.value_objects.otp_purpose import OTPPurpose


class TestOTPToken:
    """Tests para la entidad OTPToken."""

    def test_create_otp_token(self) -> None:
        """Test que verifica la creación de un OTPToken."""
        # Arrange
        email = Email("test@example.com")
        purpose = OTPPurpose.REGISTER
        otp_hash = OTPHash("abc123")
        expiration_minutes = 5

        # Act
        otp_token = OTPToken.create(
            email=email,
            purpose=purpose,
            otp_hash=otp_hash,
            expiration_minutes=expiration_minutes,
        )

        # Assert
        assert otp_token.email == email
        assert otp_token.purpose == purpose
        assert otp_token.otp_hash == otp_hash
        assert otp_token.attempts == 0
        assert otp_token.expire_at > datetime.utcnow()

    def test_is_expired_returns_false_when_not_expired(self) -> None:
        """Test que verifica is_expired retorna False cuando no está expirado."""
        # Arrange
        email = Email("test@example.com")
        purpose = OTPPurpose.REGISTER
        otp_hash = OTPHash("abc123")
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

        # Act
        is_expired = otp_token.is_expired()

        # Assert
        assert is_expired is False

    def test_is_expired_returns_true_when_expired(self) -> None:
        """Test que verifica is_expired retorna True cuando está expirado."""
        # Arrange
        email = Email("test@example.com")
        purpose = OTPPurpose.REGISTER
        otp_hash = OTPHash("abc123")
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

        # Act
        is_expired = otp_token.is_expired()

        # Assert
        assert is_expired is True

    def test_increment_attempts(self) -> None:
        """Test que verifica increment_attempts incrementa el contador."""
        # Arrange
        email = Email("test@example.com")
        purpose = OTPPurpose.REGISTER
        otp_hash = OTPHash("abc123")
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

        # Act
        otp_token.increment_attempts()

        # Assert
        assert otp_token.attempts == 1

    def test_has_reached_max_attempts_returns_true_when_reached(self) -> None:
        """Test que verifica has_reached_max_attempts retorna True cuando se alcanza el máximo."""
        # Arrange
        email = Email("test@example.com")
        purpose = OTPPurpose.REGISTER
        otp_hash = OTPHash("abc123")
        now = datetime.utcnow()
        expire_at = now + timedelta(minutes=5)

        otp_token = OTPToken(
            email=email,
            purpose=purpose,
            otp_hash=otp_hash,
            created_at=now,
            expire_at=expire_at,
            attempts=3,
        )

        # Act
        has_reached = otp_token.has_reached_max_attempts(3)

        # Assert
        assert has_reached is True

    def test_has_reached_max_attempts_returns_false_when_not_reached(self) -> None:
        """Test que verifica has_reached_max_attempts retorna False cuando no se alcanza el máximo."""
        # Arrange
        email = Email("test@example.com")
        purpose = OTPPurpose.REGISTER
        otp_hash = OTPHash("abc123")
        now = datetime.utcnow()
        expire_at = now + timedelta(minutes=5)

        otp_token = OTPToken(
            email=email,
            purpose=purpose,
            otp_hash=otp_hash,
            created_at=now,
            expire_at=expire_at,
            attempts=2,
        )

        # Act
        has_reached = otp_token.has_reached_max_attempts(3)

        # Assert
        assert has_reached is False


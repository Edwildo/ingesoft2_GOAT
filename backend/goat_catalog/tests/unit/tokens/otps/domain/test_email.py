"""Tests unitarios para el Value Object Email."""

import pytest

from goat_catalog.tokens.otps.domain.value_objects.email import Email


class TestEmail:
    """Tests para el Value Object Email."""

    def test_create_valid_email(self) -> None:
        """Test que verifica la creación de un email válido."""
        # Arrange & Act
        email = Email("test@example.com")

        # Assert
        assert email.value == "test@example.com"
        assert str(email) == "test@example.com"

    def test_email_is_lowercased(self) -> None:
        """Test que verifica que el email se convierte a minúsculas."""
        # Arrange & Act
        email = Email("TEST@EXAMPLE.COM")

        # Assert
        assert email.value == "test@example.com"

    def test_email_is_trimmed(self) -> None:
        """Test que verifica que el email se recorta de espacios."""
        # Arrange & Act
        email = Email("  test@example.com  ")

        # Assert
        assert email.value == "test@example.com"

    def test_invalid_email_raises_error(self) -> None:
        """Test que verifica que un email inválido lanza ValueError."""
        # Arrange, Act & Assert
        with pytest.raises(ValueError, match="Email inválido"):
            Email("invalid-email")

    def test_empty_email_raises_error(self) -> None:
        """Test que verifica que un email vacío lanza ValueError."""
        # Arrange, Act & Assert
        with pytest.raises(ValueError):
            Email("")

    def test_email_equality(self) -> None:
        """Test que verifica la igualdad de dos emails."""
        # Arrange
        email1 = Email("test@example.com")
        email2 = Email("test@example.com")
        email3 = Email("other@example.com")

        # Act & Assert
        assert email1 == email2
        assert email1 != email3


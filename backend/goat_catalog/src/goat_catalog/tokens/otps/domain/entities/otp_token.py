"""Entidad OTPToken - Representa un token OTP en el dominio."""

from datetime import datetime, timedelta
from typing import Optional

from ..value_objects.email import Email
from ..value_objects.otp_hash import OTPHash
from ..value_objects.otp_purpose import OTPPurpose


class OTPToken:
    """Entidad que representa un token OTP con sus reglas de negocio."""

    def __init__(
        self,
        email: Email,
        purpose: OTPPurpose,
        otp_hash: OTPHash,
        created_at: datetime,
        expire_at: datetime,
        attempts: int = 0,
    ) -> None:
        """Inicializa un OTPToken.

        Args:
            email: Email del usuario
            purpose: Propósito del OTP (REGISTER, LOGIN, etc.)
            otp_hash: Hash del código OTP
            created_at: Fecha de creación
            expire_at: Fecha de expiración
            attempts: Número de intentos de validación
        """
        self._email = email
        self._purpose = purpose
        self._otp_hash = otp_hash
        self._created_at = created_at
        self._expire_at = expire_at
        self._attempts = attempts

    @property
    def email(self) -> Email:
        """Retorna el email del token."""
        return self._email

    @property
    def purpose(self) -> OTPPurpose:
        """Retorna el propósito del token."""
        return self._purpose

    @property
    def otp_hash(self) -> OTPHash:
        """Retorna el hash del OTP."""
        return self._otp_hash

    @property
    def created_at(self) -> datetime:
        """Retorna la fecha de creación."""
        return self._created_at

    @property
    def expire_at(self) -> datetime:
        """Retorna la fecha de expiración."""
        return self._expire_at

    @property
    def attempts(self) -> int:
        """Retorna el número de intentos."""
        return self._attempts

    def is_expired(self) -> bool:
        """Verifica si el token está expirado."""
        return datetime.utcnow() >= self._expire_at

    def increment_attempts(self) -> None:
        """Incrementa el contador de intentos."""
        self._attempts += 1

    def has_reached_max_attempts(self, max_attempts: int) -> bool:
        """Verifica si se alcanzó el máximo de intentos.

        Args:
            max_attempts: Máximo de intentos permitidos

        Returns:
            True si se alcanzó el máximo, False en caso contrario
        """
        return self._attempts >= max_attempts

    @classmethod
    def create(
        cls,
        email: Email,
        purpose: OTPPurpose,
        otp_hash: OTPHash,
        expiration_minutes: int,
    ) -> "OTPToken":
        """Factory method para crear un nuevo OTPToken.

        Args:
            email: Email del usuario
            purpose: Propósito del OTP
            otp_hash: Hash del código OTP
            expiration_minutes: Minutos hasta la expiración

        Returns:
            Nueva instancia de OTPToken
        """
        now = datetime.utcnow()
        expire_at = now + timedelta(minutes=expiration_minutes)
        return cls(
            email=email,
            purpose=purpose,
            otp_hash=otp_hash,
            created_at=now,
            expire_at=expire_at,
            attempts=0,
        )


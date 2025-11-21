"""Utilidades de seguridad para hashing de OTPs."""

import hashlib
import hmac
from typing import Any

from ..config import get_settings


def hash_otp(otp: str) -> str:
    """Genera hash seguro del OTP usando HMAC-SHA256.

    Args:
        otp: Código OTP en texto plano

    Returns:
        Hash hexadecimal del OTP
    """
    settings = get_settings()
    secret = settings.otp_hash_secret.encode("utf-8")
    otp_bytes = otp.encode("utf-8")
    return hmac.new(secret, otp_bytes, hashlib.sha256).hexdigest()


def verify_otp_hash(otp: str, otp_hash: str) -> bool:
    """Verifica si un OTP coincide con su hash.

    Args:
        otp: Código OTP en texto plano
        otp_hash: Hash a verificar

    Returns:
        True si el OTP coincide con el hash, False en caso contrario
    """
    expected_hash = hash_otp(otp)
    return hmac.compare_digest(expected_hash, otp_hash)


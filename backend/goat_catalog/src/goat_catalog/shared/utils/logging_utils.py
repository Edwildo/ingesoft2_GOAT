"""Utilidades para logging seguro."""

import logging
from typing import Any

from ..config import get_settings


def sanitize_email(email: str) -> str:
    """Sanitiza un email para logging.
    
    Oculta parte del email para proteger privacidad.
    Ejemplo: user@example.com -> u***@example.com
    
    Args:
        email: Email a sanitizar
        
    Returns:
        Email sanitizado
    """
    if not email or "@" not in email:
        return "***"
    
    parts = email.split("@")
    if len(parts) != 2:
        return "***"
    
    local, domain = parts
    if len(local) <= 1:
        sanitized_local = "*"
    else:
        sanitized_local = local[0] + "*" * (len(local) - 1)
    
    return f"{sanitized_local}@{domain}"


def sanitize_hash(hash_value: str, visible_chars: int = 4) -> str:
    """Sanitiza un hash para logging.
    
    Solo muestra los primeros caracteres del hash.
    
    Args:
        hash_value: Hash a sanitizar
        visible_chars: Número de caracteres visibles (default: 4)
        
    Returns:
        Hash sanitizado
    """
    if not hash_value or len(hash_value) <= visible_chars:
        return "***"
    
    return f"{hash_value[:visible_chars]}..."


def should_log_debug() -> bool:
    """Determina si se deben loguear mensajes DEBUG.
    
    Returns:
        True si el entorno es desarrollo, False en producción
    """
    settings = get_settings()
    return settings.environment.lower() == "development"


def get_safe_logger(name: str) -> logging.Logger:
    """Obtiene un logger configurado para logging seguro.
    
    Args:
        name: Nombre del logger
        
    Returns:
        Logger configurado
    """
    logger = logging.getLogger(name)
    return logger


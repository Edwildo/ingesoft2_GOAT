"""Rate limiting middleware para FastAPI."""

from functools import lru_cache
from typing import Callable

from fastapi import Request
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from slowapi.util import get_remote_address

from ..config import get_settings


def get_client_identifier(request: Request) -> str:
    """Obtiene identificador único del cliente para rate limiting.
    
    Prioriza IP del cliente. En producción, considerar usar API keys o tokens.
    
    Args:
        request: Request de FastAPI
        
    Returns:
        String identificador único del cliente
    """
    return get_remote_address(request)


async def get_email_identifier(request: Request) -> str:
    """Obtiene identificador basado en email del request body.
    
    Usado para rate limiting por email en endpoints OTP.
    
    Args:
        request: Request de FastAPI
        
    Returns:
        Email del request o IP como fallback
    """
    try:
        body = await request.json()
        if isinstance(body, dict) and "email" in body:
            return f"email:{body['email']}"
    except Exception:
        pass
    
    return get_remote_address(request)


@lru_cache()
def get_limiter() -> Limiter:
    """Obtiene instancia singleton del rate limiter.
    
    Returns:
        Instancia configurada de Limiter
    """
    settings = get_settings()
    
    limiter = Limiter(
        key_func=get_client_identifier,
        default_limits=[f"{settings.rate_limit_per_minute}/minute"],
        storage_uri=settings.rate_limit_storage_uri,
    )
    
    return limiter


def configure_rate_limiter(app) -> None:
    """Configura rate limiting en la aplicación FastAPI.
    
    Args:
        app: Instancia de FastAPI
    """
    limiter = get_limiter()
    limiter.init_app(app)
    app.state.limiter = limiter
    app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)


def rate_limit_by_email(limit: str) -> Callable:
    """Decorador para rate limiting basado en email.
    
    Args:
        limit: Límite en formato "X/minute" o "X/hour"
        
    Returns:
        Decorador para aplicar rate limiting
    """
    limiter = get_limiter()
    return limiter.limit(limit, key_func=lambda request: get_email_identifier(request))


def rate_limit_by_ip(limit: str) -> Callable:
    """Decorador para rate limiting basado en IP.
    
    Args:
        limit: Límite en formato "X/minute" o "X/hour"
        
    Returns:
        Decorador para aplicar rate limiting
    """
    limiter = get_limiter()
    return limiter.limit(limit, key_func=get_client_identifier)


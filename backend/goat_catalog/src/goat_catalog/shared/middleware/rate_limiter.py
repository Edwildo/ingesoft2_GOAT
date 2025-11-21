"""Rate limiting middleware para FastAPI."""

from functools import lru_cache
from typing import Callable

from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from slowapi.util import get_remote_address
from starlette.requests import Request

from ..config import get_settings


def get_client_identifier(request: Request) -> str:
    """Obtiene identificador único del cliente para rate limiting.
    
    Prioriza IP del cliente. En producción, considerar usar API keys o tokens.
    
    Args:
        request: Request de Starlette/FastAPI
        
    Returns:
        String identificador único del cliente
    """
    return get_remote_address(request)


def get_email_identifier(request: Request) -> str:
    """Obtiene identificador basado en email del request body.
    
    Usado para rate limiting por email en endpoints OTP.
    Nota: slowapi llama esta función antes de que FastAPI procese el body,
    por lo que usamos IP como fallback. El rate limiting por email real
    se implementa mediante validación en el use case.
    
    Args:
        request: Request de Starlette/FastAPI
        
    Returns:
        IP del cliente (email se valida en use case)
    """
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
    # En slowapi, simplemente asignamos el limiter al app.state
    # No hay método init_app() como en Flask-Limiter
    limiter = get_limiter()
    app.state.limiter = limiter
    app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)


def rate_limit_by_email(limit: str) -> Callable:
    """Decorador para rate limiting basado en email.
    
    Nota: Debido a limitaciones de slowapi, este decorador usa IP como identificador.
    La validación real por email se hace en el use case verificando OTPs recientes.
    
    Args:
        limit: Límite en formato "X/minute" o "X/hour"
        
    Returns:
        Decorador para aplicar rate limiting
    """
    limiter = get_limiter()
    return limiter.limit(limit, key_func=get_email_identifier)


def rate_limit_by_ip(limit: str) -> Callable:
    """Decorador para rate limiting basado en IP.
    
    Args:
        limit: Límite en formato "X/minute" o "X/hour"
        
    Returns:
        Decorador para aplicar rate limiting
    """
    limiter = get_limiter()
    return limiter.limit(limit, key_func=get_client_identifier)

"""Utilidades para retry logic con exponential backoff."""

import asyncio
import logging
from functools import wraps
from typing import Any, Callable, TypeVar

logger = logging.getLogger(__name__)

T = TypeVar("T")


async def retry_with_backoff(
    func: Callable[..., Any],
    max_retries: int = 3,
    initial_delay: float = 1.0,
    max_delay: float = 60.0,
    exponential_base: float = 2.0,
    exceptions: tuple[type[Exception], ...] = (Exception,),
    *args: Any,
    **kwargs: Any,
) -> Any:
    """Ejecuta una función con retry logic y exponential backoff.
    
    Args:
        func: Función async a ejecutar
        max_retries: Número máximo de intentos (incluyendo el primero)
        initial_delay: Delay inicial en segundos
        max_delay: Delay máximo en segundos
        exponential_base: Base para el cálculo exponencial
        exceptions: Tupla de excepciones que deben trigger retry
        *args: Argumentos posicionales para la función
        **kwargs: Argumentos nombrados para la función
        
    Returns:
        Resultado de la función
        
    Raises:
        La última excepción si todos los intentos fallan
    """
    last_exception = None
    delay = initial_delay
    
    for attempt in range(max_retries):
        try:
            return await func(*args, **kwargs)
        except exceptions as e:
            last_exception = e
            if attempt < max_retries - 1:
                logger.warning(
                    f"Intento {attempt + 1}/{max_retries} falló: {type(e).__name__}. "
                    f"Reintentando en {delay:.2f} segundos..."
                )
                await asyncio.sleep(delay)
                delay = min(delay * exponential_base, max_delay)
            else:
                logger.error(
                    f"Todos los intentos ({max_retries}) fallaron. "
                    f"Última excepción: {type(e).__name__}"
                )
    
    raise last_exception


def retry_decorator(
    max_retries: int = 3,
    initial_delay: float = 1.0,
    max_delay: float = 60.0,
    exponential_base: float = 2.0,
    exceptions: tuple[type[Exception], ...] = (Exception,),
) -> Callable[[Callable[..., Any]], Callable[..., Any]]:
    """Decorador para aplicar retry logic a funciones async.
    
    Args:
        max_retries: Número máximo de intentos
        initial_delay: Delay inicial en segundos
        max_delay: Delay máximo en segundos
        exponential_base: Base para el cálculo exponencial
        exceptions: Tupla de excepciones que deben trigger retry
        
    Returns:
        Decorador
    """
    def decorator(func: Callable[..., Any]) -> Callable[..., Any]:
        @wraps(func)
        async def wrapper(*args: Any, **kwargs: Any) -> Any:
            return await retry_with_backoff(
                func,
                max_retries=max_retries,
                initial_delay=initial_delay,
                max_delay=max_delay,
                exponential_base=exponential_base,
                exceptions=exceptions,
                *args,
                **kwargs,
            )
        return wrapper
    return decorator


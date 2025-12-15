"""Dependencies de FastAPI para autenticación.

El servicio Java actúa como orquestador:
- Frontend → Java (con JWT)
- Java valida JWT y obtiene userId
- Java → Python (con userId en header X-User-Id)
"""

from uuid import UUID

from fastapi import Header, HTTPException, status


async def get_user_id_from_header(
    x_user_id: str = Header(..., alias="X-User-Id", description="ID del usuario autenticado (enviado por el servicio Java)")
) -> UUID:
    """Dependency de FastAPI para obtener el userId del header X-User-Id.
    
    El servicio Java envía el userId en el header X-User-Id después de validar el JWT.
    
    Uso:
        @router.post("/items")
        async def add_item(
            request: AddItemRequest,
            user_id: UUID = Depends(get_user_id_from_header)
        ):
            # user_id contiene el UUID del usuario autenticado
            ...
    
    Args:
        x_user_id: Header X-User-Id con el UUID del usuario (enviado por Java)
        
    Returns:
        UUID del usuario
        
    Raises:
        HTTPException: Si el header es inválido o falta
    """
    if not x_user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Header X-User-Id faltante"
        )
    
    try:
        return UUID(str(x_user_id))
    except (ValueError, TypeError) as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Formato de X-User-Id inválido: {e}"
        )


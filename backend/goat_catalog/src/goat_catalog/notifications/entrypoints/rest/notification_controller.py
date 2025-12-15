"""Controller REST para notificaciones."""

import sys
from pathlib import Path

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, EmailStr, Field

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.notifications.domain.services.notification_service import NotificationService
from goat_catalog.notifications.domain.value_objects.email import Email
from goat_catalog.notifications.infrastructure.adapters.smtp_notification_service import (
    SMTPNotificationService,
)

router = APIRouter(prefix="/api/notifications", tags=["Notifications"])


def _get_notification_service() -> NotificationService:
    """Factory para obtener instancia de NotificationService."""
    try:
        return SMTPNotificationService()
    except Exception:
        # Si hay error en la configuración SMTP, retornar servicio que no hace nada
        # El servicio continuará funcionando sin envío de emails
        return None


class OrderConfirmationRequest(BaseModel):
    """Request para enviar email de confirmación de orden."""

    email: EmailStr = Field(..., description="Email del destinatario")
    order_id: str = Field(..., description="ID de la orden")
    order_total: float = Field(..., description="Total de la orden")
    order_items_count: int = Field(..., description="Cantidad de items en la orden")


@router.post("/order-confirmation", status_code=status.HTTP_200_OK)
async def send_order_confirmation(request: OrderConfirmationRequest) -> dict:
    """Envía un email de confirmación de orden.
    
    POST /api/notifications/order-confirmation
    Body:
        {{
          "email": "user@example.com",
          "order_id": "123e4567-e89b-12d3-a456-426614174000",
          "order_total": 150000.00,
          "order_items_count": 2
        }}
    """
    import logging
    logger = logging.getLogger(__name__)
    
    logger.info(
        f"Recibida solicitud de envío de email de confirmación - "
        f"Email: {request.email}, OrderId: {request.order_id}, "
        f"Total: {request.order_total}, Items: {request.order_items_count}"
    )
    
    notification_service = _get_notification_service()
    
    if notification_service is None:
        logger.warning("Servicio de notificaciones no disponible (configuración SMTP faltante)")
        # Si el servicio de notificaciones no está disponible, retornar éxito
        # para no bloquear el flujo de creación de orden
        return {"success": False, "message": "Servicio de notificaciones no disponible - configuración SMTP faltante"}
    
    try:
        email = Email(request.email)
        logger.info(f"Enviando email de confirmación a {request.email}")
        await notification_service.send_order_confirmation_email(
            to_email=email,
            order_id=request.order_id,
            order_total=request.order_total,
            order_items_count=request.order_items_count,
        )
        logger.info(f"Email de confirmación enviado exitosamente a {request.email}")
        return {"success": True, "message": "Email de confirmación enviado exitosamente"}
    except ValueError as e:
        logger.error(f"Error de validación al enviar email: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        )
    except Exception as e:
        # No fallar la creación de orden si el email falla
        # Solo loguear el error
        logger.error(f"Error al enviar email de confirmación: {str(e)}", exc_info=True)
        return {"success": False, "message": f"Error al enviar email: {str(e)}"}

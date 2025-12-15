"""Interfaz del servicio de notificaciones (Puerto)."""

from abc import ABC, abstractmethod

from ...domain.value_objects.email import Email


class NotificationService(ABC):
    """Interfaz abstracta para el servicio de notificaciones."""

    @abstractmethod
    async def send_order_confirmation_email(
        self,
        to_email: Email,
        order_id: str,
        order_total: float,
        order_items_count: int,
    ) -> None:
        """Envía un email de confirmación de orden.

        Args:
            to_email: Email destino
            order_id: ID de la orden
            order_total: Total de la orden
            order_items_count: Cantidad de items en la orden

        Raises:
            Exception: Si hay un error al enviar el email
        """
        raise NotImplementedError

"""Implementación SMTP del servicio de notificaciones."""

import logging
import ssl
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

import aiosmtplib

import sys
from pathlib import Path

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.config import get_settings
from goat_catalog.shared.utils.logging_utils import sanitize_email, should_log_debug
from goat_catalog.shared.utils.retry import retry_with_backoff
from ...domain.services.notification_service import NotificationService
from ...domain.value_objects.email import Email

logger = logging.getLogger(__name__)


class SMTPNotificationService(NotificationService):
    """Implementación SMTP del servicio de notificaciones."""

    def __init__(self) -> None:
        """Inicializa el servicio SMTP."""
        self._settings = get_settings()
        logger.info(
            f"SMTPNotificationService inicializado - "
            f"Host: {self._settings.smtp_host}, Port: {self._settings.smtp_port}, "
            f"User: {self._settings.smtp_user[:3] + '***' if self._settings.smtp_user else 'None'}, "
            f"From: {self._settings.email_from}"
        )

    def _create_order_confirmation_html(self, order_id: str, order_total: float, order_items_count: int) -> str:
        """Crea el cuerpo del email HTML para confirmación de orden."""
        styles = """
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }
        .container {
            background-color: #f9f9f9;
            border-radius: 8px;
            padding: 30px;
            margin: 20px 0;
        }
        .header {
            background-color: #007bff;
            color: white;
            padding: 20px;
            border-radius: 6px;
            text-align: center;
            margin-bottom: 20px;
        }
        .order-info {
            background-color: #ffffff;
            padding: 20px;
            border-radius: 6px;
            margin: 20px 0;
        }
        .order-id {
            font-size: 24px;
            font-weight: bold;
            color: #007bff;
            text-align: center;
            margin: 20px 0;
        }
        .footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            font-size: 12px;
            color: #666;
        }
        """
        
        return f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>{styles}</style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>¡Orden Confirmada!</h1>
        </div>
        <p>Gracias por tu compra en GOAT. Tu orden ha sido confirmada exitosamente.</p>
        <div class="order-info">
            <div class="order-id">{order_id[:8].upper()}</div>
            <p><strong>Número de orden:</strong> {order_id}</p>
            <p><strong>Total:</strong> ${order_total:,.0f} COP</p>
            <p><strong>Items:</strong> {order_items_count}</p>
        </div>
        <p>Recibirás una notificación cuando tu orden sea enviada.</p>
    </div>
    <div class="footer">
        <p>Este es un mensaje automático, por favor no respondas a este correo.</p>
        <p>GOAT - Tu plataforma de sneakers</p>
    </div>
</body>
</html>"""

    def _create_order_confirmation_plain_text(self, order_id: str, order_total: float, order_items_count: int) -> str:
        """Crea el cuerpo del email en texto plano para confirmación de orden."""
        return f"""¡Orden Confirmada!

Gracias por tu compra en GOAT. Tu orden ha sido confirmada exitosamente.

Número de orden: {order_id}
Total: ${order_total:,.0f} COP
Items: {order_items_count}

Recibirás una notificación cuando tu orden sea enviada.

Este es un mensaje automático, por favor no respondas a este correo.
GOAT - Tu plataforma de sneakers"""

    def _create_ssl_context(self) -> ssl.SSLContext | None:
        """Crea el contexto SSL para la conexión SMTP."""
        if not self._settings.smtp_validate_cert:
            if should_log_debug():
                logger.debug("Validación de certificados SSL deshabilitada (solo desarrollo)")
            return None
        
        context = ssl.create_default_context()
        if should_log_debug():
            logger.debug("Validación de certificados SSL habilitada")
        return context

    async def _send_email_internal(
        self,
        to_email: Email,
        message: MIMEMultipart,
    ) -> None:
        """Envía el email usando SMTP (método interno para retry)."""
        smtp = None
        sanitized_email = sanitize_email(to_email.value)
        
        try:
            use_start_tls = self._settings.smtp_port == 587
            use_tls_direct = self._settings.smtp_port == 465
            logger.info(f"Enviando email de confirmación de orden a {sanitized_email}")
            logger.info(f"SMTP Server: {self._settings.smtp_host}:{self._settings.smtp_port}")
            logger.info(f"SMTP Config - STARTTLS: {use_start_tls}, TLS Direct: {use_tls_direct}")
            logger.info(f"SMTP User: {self._settings.smtp_user[:3] + '***' if self._settings.smtp_user else 'None'}")

            ssl_context = self._create_ssl_context()
            
            smtp = aiosmtplib.SMTP(
                hostname=self._settings.smtp_host,
                port=self._settings.smtp_port,
                use_tls=use_tls_direct,
                start_tls=use_start_tls,
                tls_context=ssl_context,
            )

            logger.info("Conectando al servidor SMTP...")
            await smtp.connect()
            logger.info(f"Conexión SMTP establecida: {smtp.is_connected}")

            if self._settings.smtp_user and self._settings.smtp_password:
                logger.info("Autenticando con servidor SMTP...")
                await smtp.login(
                    self._settings.smtp_user,
                    self._settings.smtp_password,
                )
                logger.info("Autenticación SMTP exitosa")
            else:
                logger.warning("No se proporcionaron credenciales SMTP (smtp_user o smtp_password vacíos)")

            logger.info("Enviando email...")
            
            errors, response = await smtp.send_message(message)
            
            if errors:
                error_details = {recipient: str(error)[:200] for recipient, error in errors.items()}
                logger.error(f"Errores al enviar email SMTP a {sanitized_email}: {len(errors)} error(es). Detalles: {error_details}")
                raise Exception(f"Error al enviar email: {len(errors)} error(es)")
            
            logger.info(f"Email de confirmación de orden enviado exitosamente a {sanitized_email}")

        except Exception as e:
            logger.error(f"Error al enviar email de confirmación a {sanitized_email}: {type(e).__name__}: {str(e)}", exc_info=True)
            raise
        finally:
            if smtp and smtp.is_connected:
                try:
                    await smtp.quit()
                except Exception:
                    pass

    async def send_order_confirmation_email(
        self,
        to_email: Email,
        order_id: str,
        order_total: float,
        order_items_count: int,
    ) -> None:
        """Envía un email de confirmación de orden usando SMTP con retry logic."""
        plain_text = self._create_order_confirmation_plain_text(order_id, order_total, order_items_count)
        html_content = self._create_order_confirmation_html(order_id, order_total, order_items_count)

        message = MIMEMultipart("alternative")
        message["Subject"] = f"Confirmación de Orden GOAT - {order_id[:8].upper()}"
        message["From"] = self._settings.email_from
        message["To"] = to_email.value

        part1 = MIMEText(plain_text, "plain", "utf-8")
        part2 = MIMEText(html_content, "html", "utf-8")

        message.attach(part1)
        message.attach(part2)

        sanitized_email = sanitize_email(to_email.value)
        
        if should_log_debug():
            logger.debug(f"Iniciando envío de email de confirmación de orden a {sanitized_email}")

        await retry_with_backoff(
            self._send_email_internal,
            max_retries=self._settings.email_max_retries,
            initial_delay=self._settings.email_retry_initial_delay,
            max_delay=self._settings.email_retry_max_delay,
            exceptions=(
                aiosmtplib.SMTPConnectError,
                aiosmtplib.SMTPException,
            ),
            to_email=to_email,
            message=message,
        )

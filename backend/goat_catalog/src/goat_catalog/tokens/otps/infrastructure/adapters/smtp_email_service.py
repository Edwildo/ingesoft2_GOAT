"""Implementación SMTP del servicio de email."""

import logging
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
from ...domain.exceptions.email_send_exception import (
    EmailAuthenticationException,
    EmailConnectionException,
    EmailDeliveryException,
)
from ...domain.services.email_service import EmailService
from ...domain.value_objects.email import Email
from ...domain.value_objects.otp_purpose import OTPPurpose

logger = logging.getLogger(__name__)


class SMTPEmailService(EmailService):
    """Implementación SMTP del servicio de email."""

    def __init__(self) -> None:
        """Inicializa el servicio SMTP."""
        self._settings = get_settings()

    def _get_purpose_message(self, purpose: OTPPurpose) -> str:
        """Obtiene el mensaje según el propósito del OTP."""
        purpose_messages = {
            OTPPurpose.REGISTER: "registro",
            OTPPurpose.LOGIN: "inicio de sesión",
            OTPPurpose.RESET_PASSWORD: "restablecer contraseña",
            OTPPurpose.EMAIL_CONFIRMATION: "confirmar email",
        }
        return purpose_messages.get(purpose, "autenticación")

    def _create_plain_text_body(self, otp_code: str, purpose_message: str) -> str:
        """Crea el cuerpo del email en texto plano."""
        expiration = self._settings.otp_expiration_minutes
        return f"""Código de verificación GOAT

Tu código de verificación para {purpose_message} es:

{otp_code}

Este código expira en {expiration} minutos.

Si no solicitaste este código, ignora este mensaje."""

    def _get_html_styles(self) -> str:
        """Retorna los estilos CSS para el email HTML."""
        return """
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
        .otp-code {
            font-size: 32px;
            font-weight: bold;
            text-align: center;
            letter-spacing: 8px;
            color: #007bff;
            background-color: #ffffff;
            padding: 20px;
            border-radius: 6px;
            margin: 20px 0;
        }
        .footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            font-size: 12px;
            color: #666;
        }"""

    def _create_html_body(self, otp_code: str, purpose_message: str) -> str:
        """Crea el cuerpo del email en formato HTML."""
        expiration = self._settings.otp_expiration_minutes
        styles = self._get_html_styles()
        
        return f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>{styles}
    </style>
</head>
<body>
    <div class="container">
        <h2>Código de verificación GOAT</h2>
        <p>Tu código de verificación para {purpose_message} es:</p>
        <div class="otp-code">{otp_code}</div>
        <p>Este código expira en <strong>{expiration} minutos</strong>.</p>
        <p>Si no solicitaste este código, ignora este mensaje.</p>
    </div>
    <div class="footer">
        <p>Este es un mensaje automático, por favor no respondas a este correo.</p>
    </div>
</body>
</html>"""

    def _create_email_body(self, otp_code: str, purpose: OTPPurpose) -> tuple[str, str]:
        """Crea el cuerpo del email en texto plano y HTML."""
        purpose_message = self._get_purpose_message(purpose)
        plain_text = self._create_plain_text_body(otp_code, purpose_message)
        html_content = self._create_html_body(otp_code, purpose_message)
        return plain_text, html_content

    async def _send_email_internal(
        self,
        to_email: Email,
        message: MIMEMultipart,
        purpose: OTPPurpose,
    ) -> None:
        """Envía el email usando SMTP (método interno para retry)."""
        smtp = None
        sanitized_email = sanitize_email(to_email.value)
        
        try:
            if should_log_debug():
                use_start_tls = self._settings.smtp_port == 587
                use_tls_direct = self._settings.smtp_port == 465
                logger.debug(
                    f"Enviando email OTP a {sanitized_email} "
                    f"(proposito: {purpose.value})"
                )
                logger.debug(f"SMTP Server: {self._settings.smtp_host}:{self._settings.smtp_port}")
                logger.debug(f"Conexion: STARTTLS={use_start_tls}, TLS Directo={use_tls_direct}")

            # Conectar al servidor SMTP
            # IMPORTANTE: use_tls y start_tls son mutuamente excluyentes
            smtp = aiosmtplib.SMTP(
                hostname=self._settings.smtp_host,
                port=self._settings.smtp_port,
                use_tls=use_tls_direct,  # Solo para puerto 465
                start_tls=use_start_tls,  # Solo para puerto 587
            )

            await smtp.connect()
            
            if should_log_debug():
                logger.debug(f"Conexion SMTP establecida: {smtp.is_connected}")

            if self._settings.smtp_user and self._settings.smtp_password:
                if should_log_debug():
                    logger.debug("Autenticando con servidor SMTP...")
                await smtp.login(
                    self._settings.smtp_user,
                    self._settings.smtp_password,
                )
                if should_log_debug():
                    sanitized_user = sanitize_email(self._settings.smtp_user)
                    logger.debug(f"Autenticacion SMTP exitosa para: {sanitized_user}")

            if should_log_debug():
                logger.debug("Enviando email...")
            
            errors, response = await smtp.send_message(message)
            
            if errors:
                error_details = {
                    sanitize_email(recipient): str(error)[:100]
                    for recipient, error in errors.items()
                }
                logger.error(
                    f"Errores al enviar email SMTP a {sanitized_email}: {len(errors)} error(es)",
                    extra={"error_details": error_details} if should_log_debug() else {},
                )
                raise EmailDeliveryException(
                    f"Error al enviar email: {len(errors)} error(es)",
                    recipient=sanitized_email,
                )
            
            logger.info(f"Email OTP enviado exitosamente a {sanitized_email} (proposito: {purpose.value})")

        except aiosmtplib.SMTPAuthenticationError as e:
            logger.error(
                f"Error de autenticacion SMTP al enviar email a {sanitized_email}",
                exc_info=should_log_debug(),
                extra={"smtp_host": self._settings.smtp_host, "smtp_port": self._settings.smtp_port},
            )
            raise EmailAuthenticationException(
                f"Error de autenticacion SMTP: {type(e).__name__}"
            ) from e
        except aiosmtplib.SMTPConnectError as e:
            logger.error(
                f"Error de conexion SMTP al enviar email a {sanitized_email}",
                exc_info=should_log_debug(),
                extra={"smtp_host": self._settings.smtp_host, "smtp_port": self._settings.smtp_port},
            )
            raise EmailConnectionException(
                f"Error de conexion SMTP: {type(e).__name__}"
            ) from e
        except (EmailAuthenticationException, EmailConnectionException, EmailDeliveryException):
            raise
        except aiosmtplib.SMTPException as e:
            logger.error(
                f"Error SMTP al enviar email a {sanitized_email}",
                exc_info=should_log_debug(),
                extra={"smtp_error_type": type(e).__name__},
            )
            raise EmailDeliveryException(
                f"Error SMTP: {type(e).__name__}",
                recipient=sanitized_email,
            ) from e
        except Exception as e:
            logger.error(
                f"Error inesperado al enviar email OTP a {sanitized_email}",
                exc_info=should_log_debug(),
                extra={"error_type": type(e).__name__},
            )
            raise EmailDeliveryException(
                f"Error inesperado: {type(e).__name__}",
                recipient=sanitized_email,
            ) from e
        finally:
            if smtp and smtp.is_connected:
                try:
                    await smtp.quit()
                    if should_log_debug():
                        logger.debug("Conexion SMTP cerrada correctamente")
                except Exception as e:
                    logger.warning(f"Advertencia al cerrar conexion SMTP: {type(e).__name__}")

    async def send_otp_email(
        self,
        to_email: Email,
        otp_code: str,
        purpose: OTPPurpose,
    ) -> None:
        """Envía un email con el código OTP usando SMTP con retry logic.
        
        Args:
            to_email: Email del destinatario
            otp_code: Código OTP a enviar
            purpose: Propósito del OTP
            
        Raises:
            EmailAuthenticationException: Si hay error de autenticación SMTP
            EmailConnectionException: Si hay error de conexión SMTP
            EmailDeliveryException: Si hay error al entregar el email
        """
        plain_text, html_content = self._create_email_body(otp_code, purpose)
        purpose_message = self._get_purpose_message(purpose)

        message = MIMEMultipart("alternative")
        message["Subject"] = f"Código de verificación GOAT - {purpose_message.capitalize()}"
        message["From"] = self._settings.email_from
        message["To"] = to_email.value

        part1 = MIMEText(plain_text, "plain", "utf-8")
        part2 = MIMEText(html_content, "html", "utf-8")

        message.attach(part1)
        message.attach(part2)

        sanitized_email = sanitize_email(to_email.value)
        
        if should_log_debug():
            logger.debug(
                f"Iniciando envio de email OTP a {sanitized_email} "
                f"(proposito: {purpose.value})"
            )

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
            purpose=purpose,
        )


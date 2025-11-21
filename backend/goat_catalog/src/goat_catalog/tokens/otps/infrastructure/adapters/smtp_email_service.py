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

    def _create_email_body(self, otp_code: str, purpose: OTPPurpose) -> tuple[str, str]:
        """Crea el cuerpo del email en texto plano y HTML."""
        purpose_message = self._get_purpose_message(purpose)
        
        plain_text = f"""
Código de verificación GOAT

Tu código de verificación para {purpose_message} es:

{otp_code}

Este código expira en {self._settings.otp_expiration_minutes} minutos.

Si no solicitaste este código, ignora este mensaje.
        """.strip()

        html_content = f"""
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {{
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }}
        .container {{
            background-color: #f9f9f9;
            border-radius: 8px;
            padding: 30px;
            margin: 20px 0;
        }}
        .otp-code {{
            font-size: 32px;
            font-weight: bold;
            text-align: center;
            letter-spacing: 8px;
            color: #007bff;
            background-color: #ffffff;
            padding: 20px;
            border-radius: 6px;
            margin: 20px 0;
        }}
        .footer {{
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            font-size: 12px;
            color: #666;
        }}
    </style>
</head>
<body>
    <div class="container">
        <h2>Código de verificación GOAT</h2>
        <p>Tu código de verificación para {purpose_message} es:</p>
        <div class="otp-code">{otp_code}</div>
        <p>Este código expira en <strong>{self._settings.otp_expiration_minutes} minutos</strong>.</p>
        <p>Si no solicitaste este código, ignora este mensaje.</p>
    </div>
    <div class="footer">
        <p>Este es un mensaje automático, por favor no respondas a este correo.</p>
    </div>
</body>
</html>
        """.strip()

        return plain_text, html_content

    async def send_otp_email(
        self,
        to_email: Email,
        otp_code: str,
        purpose: OTPPurpose,
    ) -> None:
        """Envía un email con el código OTP usando SMTP."""
        smtp = None
        try:
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

            # Determinar tipo de TLS según el puerto
            # Puerto 587 usa STARTTLS (conexión normal, luego upgrade a TLS)
            # Puerto 465 usa TLS directo (conexión segura desde el inicio)
            use_start_tls = self._settings.smtp_port == 587
            use_tls_direct = self._settings.smtp_port == 465

            logger.info(
                f"📧 Iniciando envío de email OTP a {to_email.value} "
                f"(propósito: {purpose.value})"
            )
            logger.info(
                f"   SMTP Server: {self._settings.smtp_host}:{self._settings.smtp_port}"
            )
            logger.info(
                f"   Conexión: STARTTLS={use_start_tls}, TLS Directo={use_tls_direct}"
            )
            logger.info(f"   From: {self._settings.email_from}")
            logger.info(f"   To: {to_email.value}")

            # Conectar al servidor SMTP
            # IMPORTANTE: use_tls y start_tls son mutuamente excluyentes
            smtp = aiosmtplib.SMTP(
                hostname=self._settings.smtp_host,
                port=self._settings.smtp_port,
                use_tls=use_tls_direct,  # Solo para puerto 465
                start_tls=use_start_tls,  # Solo para puerto 587
            )

            # Conectar
            logger.debug("   Conectando al servidor SMTP...")
            await smtp.connect()
            logger.info(f"   ✅ Conexión SMTP establecida: {smtp.is_connected}")

            # Autenticar si se proporcionan credenciales
            if self._settings.smtp_user and self._settings.smtp_password:
                logger.debug("   Autenticando con servidor SMTP...")
                await smtp.login(
                    self._settings.smtp_user,
                    self._settings.smtp_password,
                )
                logger.info(
                    f"   ✅ Autenticación SMTP exitosa para: {self._settings.smtp_user}"
                )

            # Enviar el email
            logger.debug("   Enviando email...")
            errors, response = await smtp.send_message(message)
            
            # Logging de respuesta SMTP
            logger.info(f"   📬 Respuesta SMTP completa: {response}")
            
            if errors:
                logger.error(
                    f"   ❌ Errores al enviar email SMTP: {errors}"
                )
                for recipient, error in errors.items():
                    logger.error(f"      - {recipient}: {error}")
                raise Exception(f"Error al enviar email: {errors}")
            else:
                logger.info(
                    f"   ✅ Email enviado exitosamente sin errores"
                )

            logger.info(
                f"✅ Email OTP enviado exitosamente a {to_email.value} "
                f"(propósito: {purpose.value})"
            )

        except aiosmtplib.SMTPAuthenticationError as e:
            logger.error(
                f"❌ Error de autenticación SMTP al enviar email a {to_email.value}: {e}",
                exc_info=True,
            )
            raise
        except aiosmtplib.SMTPConnectError as e:
            logger.error(
                f"❌ Error de conexión SMTP al enviar email a {to_email.value}: {e}",
                exc_info=True,
            )
            raise
        except aiosmtplib.SMTPException as e:
            logger.error(
                f"❌ Error SMTP al enviar email a {to_email.value}: {e}",
                exc_info=True,
            )
            raise
        except Exception as e:
            logger.error(
                f"❌ Error inesperado al enviar email OTP a {to_email.value}: {e}",
                exc_info=True,
            )
            raise
        finally:
            # Cerrar conexión SMTP
            if smtp and smtp.is_connected:
                try:
                    await smtp.quit()
                    logger.debug("   Conexión SMTP cerrada correctamente")
                except Exception as e:
                    logger.warning(f"   Advertencia al cerrar conexión SMTP: {e}")


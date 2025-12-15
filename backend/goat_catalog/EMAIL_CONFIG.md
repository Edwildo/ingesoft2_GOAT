# Configuración de Email para Notificaciones

Para que el sistema pueda enviar emails de confirmación de orden, necesitas configurar las credenciales SMTP.

## Configuración mediante variables de entorno

Crea un archivo `.env` en la raíz del proyecto `backend/goat_catalog/` con las siguientes variables:

```env
# Configuración SMTP
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu-email@gmail.com
SMTP_PASSWORD=tu-contraseña-de-aplicación
EMAIL_FROM=noreply@goat.com
SMTP_VALIDATE_CERT=true
```

## Ejemplos de configuración para diferentes proveedores

### Gmail
```env
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu-email@gmail.com
SMTP_PASSWORD=tu-contraseña-de-aplicación  # Necesitas generar una contraseña de aplicación
EMAIL_FROM=tu-email@gmail.com
```

**Nota:** Para Gmail, necesitas generar una "Contraseña de aplicación" desde tu cuenta de Google:
1. Ve a tu cuenta de Google → Seguridad
2. Activa la verificación en 2 pasos
3. Genera una "Contraseña de aplicación"
4. Usa esa contraseña en `SMTP_PASSWORD`

### Outlook/Hotmail
```env
SMTP_HOST=smtp-mail.outlook.com
SMTP_PORT=587
SMTP_USER=tu-email@outlook.com
SMTP_PASSWORD=tu-contraseña
EMAIL_FROM=tu-email@outlook.com
```

### SendGrid
```env
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USER=apikey
SMTP_PASSWORD=tu-api-key-de-sendgrid
EMAIL_FROM=noreply@tudominio.com
```

### Mailtrap (para desarrollo/testing)
```env
SMTP_HOST=smtp.mailtrap.io
SMTP_PORT=2525
SMTP_USER=tu-usuario-mailtrap
SMTP_PASSWORD=tu-contraseña-mailtrap
EMAIL_FROM=noreply@goat.com
SMTP_VALIDATE_CERT=false
```

## Verificación

Después de configurar, reinicia el servicio Python. Los logs deberían mostrar:

```
SMTPNotificationService inicializado - Host: smtp.gmail.com, Port: 587, ...
```

Si ves errores de conexión o autenticación, verifica:
1. Que las credenciales sean correctas
2. Que el puerto sea el correcto (587 para STARTTLS, 465 para TLS directo)
3. Que el firewall no bloquee la conexión
4. Para Gmail, que hayas generado una contraseña de aplicación

## Logs

El sistema registra información detallada sobre el envío de emails. Revisa los logs del servicio Python para diagnosticar problemas.






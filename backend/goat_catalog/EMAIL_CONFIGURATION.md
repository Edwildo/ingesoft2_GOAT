# 📧 Configuración de Email para OTP

Este documento explica cómo configurar el envío de correos electrónicos para la verificación OTP.

## 🔧 Variables de Entorno Requeridas

Agrega las siguientes variables a tu archivo `.env`:

```env
# Email Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu-email@gmail.com
SMTP_PASSWORD=tu-app-password
EMAIL_FROM=noreply@goat.com
```

## 📋 Proveedores de Email Comunes

### Gmail

1. **Habilitar verificación en 2 pasos:**
   - Ve a tu cuenta de Google → Seguridad
   - Habilita la verificación en 2 pasos

2. **Generar una contraseña de aplicación:**
   - Ve a: https://myaccount.google.com/apppasswords
   - Selecciona "App" → "Mail" → "Otro (nombre personalizado)"
   - Ingresa un nombre (ej: "GOAT OTP Service")
   - Copia la contraseña generada (16 caracteres sin espacios)

3. **Configuración en `.env`:**
   ```env
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USER=tu-email@gmail.com
   SMTP_PASSWORD=abcd efgh ijkl mnop  # Contraseña de aplicación de 16 caracteres (sin espacios)
   EMAIL_FROM=tu-email@gmail.com
   ```

### Outlook/Hotmail

```env
SMTP_HOST=smtp-mail.outlook.com
SMTP_PORT=587
SMTP_USER=tu-email@outlook.com
SMTP_PASSWORD=tu-contraseña
EMAIL_FROM=tu-email@outlook.com
```

### SendGrid

1. **Crear cuenta y obtener API Key:**
   - Regístrate en https://sendgrid.com
   - Ve a Settings → API Keys
   - Crea un nuevo API Key con permisos de "Mail Send"

2. **Configuración en `.env`:**
   ```env
   SMTP_HOST=smtp.sendgrid.net
   SMTP_PORT=587
   SMTP_USER=apikey
   SMTP_PASSWORD=tu-api-key-de-sendgrid
   EMAIL_FROM=noreply@goat.com  # Debe estar verificado en SendGrid
   ```

### AWS SES (Simple Email Service)

```env
SMTP_HOST=email-smtp.us-east-1.amazonaws.com  # Cambia según tu región
SMTP_PORT=587
SMTP_USER=tu-access-key-id
SMTP_PASSWORD=tu-secret-access-key
EMAIL_FROM=noreply@goat.com  # Debe estar verificado en SES
```

### Mailgun

```env
SMTP_HOST=smtp.mailgun.org
SMTP_PORT=587
SMTP_USER=postmaster@tu-dominio.mailgun.org
SMTP_PASSWORD=tu-password-de-mailgun
EMAIL_FROM=noreply@goat.com
```

## 🔒 Seguridad

### ⚠️ IMPORTANTE:

1. **NUNCA commitees el archivo `.env`** a control de versiones
2. **Usa contraseñas de aplicación**, no tu contraseña principal
3. **Para producción**, usa servicios especializados como SendGrid, AWS SES o Mailgun
4. **Limita los permisos** de la cuenta de email utilizada

## 🧪 Probar la Configuración

Una vez configurado, prueba el envío de emails:

```bash
# Generar un OTP (esto enviará un email)
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tu-email@example.com",
    "purpose": "REGISTER"
  }'
```

## 🚨 Solución de Problemas

### Error: "SMTPAuthenticationError"

- Verifica que `SMTP_USER` y `SMTP_PASSWORD` sean correctos
- Para Gmail, asegúrate de usar una **contraseña de aplicación**, no tu contraseña normal
- Verifica que la verificación en 2 pasos esté habilitada (Gmail)

### Error: "Connection refused" o timeout

- Verifica que `SMTP_HOST` y `SMTP_PORT` sean correctos
- Asegúrate de que tu firewall no esté bloqueando el puerto SMTP
- Para Gmail, verifica que "Less secure app access" esté deshabilitado (usa App Passwords)

### Los emails no se envían pero no hay error

- Revisa los logs del servidor para mensajes de advertencia
- Verifica que `EMAIL_FROM` esté configurado correctamente
- Si usas SendGrid/SES, verifica que el dominio esté verificado

### Modo Desarrollo (sin enviar emails reales)

Si no quieres enviar emails en desarrollo, simplemente no configures las variables SMTP. El servicio funcionará normalmente pero solo mostrará un warning en los logs:

```
WARNING: Servicio de email no configurado. OTP generado pero no enviado.
```

## 📝 Notas

- El servicio de email es **opcional**. Si no está configurado, el OTP se genera y guarda correctamente, pero no se envía por email
- Los errores al enviar emails **no bloquean** la generación del OTP
- Todos los errores se registran en los logs para depuración


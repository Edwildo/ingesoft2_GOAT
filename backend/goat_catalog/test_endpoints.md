# 🧪 Comandos curl para probar los endpoints

Base URL: `http://localhost:8082`

---

## 1. Health Check

```bash
curl -X GET "http://localhost:8082/health" \
  -H "Content-Type: application/json"
```

**Respuesta esperada:**
```json
{
  "status": "ok",
  "service": "goat-catalog-service",
  "version": "1.0.0"
}
```

---

## 2. Generar OTP para REGISTER

```bash
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "REGISTER"
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "OTP generado correctamente",
  "expires_in_minutes": 5
}
```

---

## 3. Generar OTP para LOGIN

```bash
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "LOGIN"
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "OTP generado correctamente",
  "expires_in_minutes": 5
}
```

---

## 4. Generar OTP para RESET_PASSWORD

```bash
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "RESET_PASSWORD"
  }'
```

---

## 5. Generar OTP para EMAIL_CONFIRMATION

```bash
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "EMAIL_CONFIRMATION"
  }'
```

---

## 6. Validar OTP

**⚠️ IMPORTANTE:** Reemplaza `123456` con el código OTP real que recibiste.

```bash
curl -X POST "http://localhost:8082/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "otp": "123456",
    "purpose": "REGISTER"
  }'
```

**Respuesta esperada (OTP válido):**
```json
{
  "success": true,
  "message": "OTP válido",
  "valid": true
}
```

**Respuesta esperada (OTP inválido):**
```json
{
  "detail": "OTP inválido"
}
```

**Respuesta esperada (OTP expirado):**
```json
{
  "detail": "OTP ha expirado"
}
```

**Respuesta esperada (máximo de intentos):**
```json
{
  "detail": "Máximo de intentos alcanzado"
}
```

---

## 🔄 Flujo completo de ejemplo

### Paso 1: Generar OTP
```bash
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "purpose": "REGISTER"
  }'
```

### Paso 2: Obtener el OTP (por ahora solo se genera, no se envía por email)
⚠️ **Nota:** Por ahora el OTP no se envía por email, tendrás que obtenerlo directamente de MongoDB o logs.

### Paso 3: Validar el OTP
```bash
curl -X POST "http://localhost:8082/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otp": "123456",
    "purpose": "REGISTER"
  }'
```

---

## 📋 Propósitos válidos de OTP

- `REGISTER` - Para registro de usuarios
- `LOGIN` - Para login de usuarios
- `RESET_PASSWORD` - Para recuperación de contraseña
- `EMAIL_CONFIRMATION` - Para confirmación de email

---

## 7. Verificar Estado de Confirmación de Email

```bash
curl -X GET "http://localhost:8082/api/auth/verify-email-status?email=user@example.com" \
  -H "Content-Type: application/json"
```

**Respuesta esperada:**
```json
{
  "confirmed": true,
  "email": "user@example.com"
}
```

---

## 8. Confirmar Email (Verificar Estado)

```bash
curl -X GET "http://localhost:8082/api/auth/confirm-email?email=user@example.com" \
  -H "Content-Type: application/json"
```

**Respuesta exitosa (email confirmado):**
```json
{
  "email": "user@example.com",
  "confirmed": true,
  "message": "Email confirmado exitosamente"
}
```

**Respuesta si no está confirmado:**
```json
{
  "email": "user@example.com",
  "confirmed": false,
  "message": "El email aún no ha sido confirmado (OTP no verificado)"
}
```

---

## 🔄 Flujo completo de confirmación de email

### Paso 1: Generar OTP para confirmación de email
```bash
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "purpose": "EMAIL_CONFIRMATION"
  }'
```

### Paso 2: Validar el OTP (esto marcará el email como confirmado)
```bash
curl -X POST "http://localhost:8082/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "otp": "123456",
    "purpose": "EMAIL_CONFIRMATION"
  }'
```

### Paso 3: Verificar el estado de confirmación
```bash
curl -X GET "http://localhost:8082/api/auth/verify-email-status?email=user@example.com"
```

### Paso 4: Confirmar email (verificar estado)
```bash
curl -X GET "http://localhost:8082/api/auth/confirm-email?email=user@example.com"
```

---

## 🚨 Códigos de estado HTTP

- `200 OK` - OTP válido, email confirmado, o consulta exitosa
- `201 Created` - OTP generado correctamente
- `400 Bad Request` - OTP inválido o datos incorrectos
- `410 Gone` - OTP expirado
- `429 Too Many Requests` - Máximo de intentos alcanzado
- `500 Internal Server Error` - Error interno del servidor

---

## 💡 Notas

- Los OTPs expiran después de 5 minutos (configurable en `.env`)
- Máximo de 3 intentos de validación por OTP (configurable en `.env`)
- Los OTPs se almacenan como hash (nunca en texto plano)
- Los OTPs expirados se eliminan automáticamente mediante TTL de MongoDB


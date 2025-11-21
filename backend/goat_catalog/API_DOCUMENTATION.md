# 📚 Documentación de API - GOAT Catalog & Authentication Service

## 🎯 Flujos Preparados

Este servicio Python está preparado para soportar los siguientes flujos de autenticación y gestión de tokens:

### 1. 🔐 Flujo de Registro de Usuario
**Propósito:** Registrar nuevos usuarios en el sistema

1. Usuario solicita registro → **Servicio Java Identity**
2. Java Identity llama → **Python Service: POST /api/auth/otp** (purpose: `REGISTER`)
3. Python genera OTP → Guarda en MongoDB (TTL) → Envía por email
4. Usuario ingresa OTP → **Servicio Java Identity**
5. Java Identity llama → **Python Service: POST /api/auth/verify** (purpose: `REGISTER`)
6. Python valida OTP → Retorna resultado

### 2. 🔑 Flujo de Login de Usuario
**Propósito:** Autenticar usuarios existentes

1. Usuario solicita login → **Servicio Java Identity**
2. Java Identity llama → **Python Service: POST /api/auth/otp** (purpose: `LOGIN`)
3. Python genera OTP → Guarda en MongoDB (TTL) → Envía por email
4. Usuario ingresa OTP → **Servicio Java Identity**
5. Java Identity llama → **Python Service: POST /api/auth/verify** (purpose: `LOGIN`)
6. Python valida OTP → Retorna resultado

### 3. 📧 Flujo de Confirmación de Email
**Propósito:** Confirmar que el usuario es dueño del email

1. Usuario solicita confirmación de email → **Servicio Java Identity**
2. Java Identity llama → **Python Service: POST /api/auth/otp** (purpose: `EMAIL_CONFIRMATION`)
3. Python genera OTP → Guarda en MongoDB (TTL) → Envía por email
4. Usuario ingresa OTP → **Servicio Java Identity**
5. Java Identity llama → **Python Service: POST /api/auth/verify** (purpose: `EMAIL_CONFIRMATION`)
6. Python valida OTP → **Marca el email como confirmado** → Retorna resultado
7. Java Identity puede verificar estado → **Python Service: GET /api/auth/verify-email-status**

### 4. 🔄 Flujo de Recuperación de Contraseña
**Propósito:** Permitir a usuarios recuperar acceso a su cuenta

1. Usuario solicita reset de contraseña → **Servicio Java Identity**
2. Java Identity llama → **Python Service: POST /api/auth/otp** (purpose: `RESET_PASSWORD`)
3. Python genera OTP → Guarda en MongoDB (TTL) → Envía por email
4. Usuario ingresa OTP → **Servicio Java Identity**
5. Java Identity llama → **Python Service: POST /api/auth/verify** (purpose: `RESET_PASSWORD`)
6. Python valida OTP → Retorna resultado

---

## 📋 Endpoints Disponibles

### Base URL
```
http://localhost:8082
```

### Documentación Interactiva
- **Swagger UI:** `http://localhost:8082/docs`
- **ReDoc:** `http://localhost:8082/redoc`

---

## 🔍 Endpoints Detallados

### 1. Health Check

**Endpoint:** `GET /health`

**Descripción:** Verifica que el servicio esté funcionando correctamente.

**Request:**
```bash
curl -X GET "http://localhost:8082/health"
```

**Response (200 OK):**
```json
{
  "status": "ok",
  "service": "goat-catalog-service",
  "version": "1.0.0"
}
```

---

### 2. Generar OTP

**Endpoint:** `POST /api/auth/otp`

**Descripción:** Genera un código OTP y lo almacena en MongoDB con TTL. El OTP se envía por email (cuando se implemente el envío).

**Request:**
```bash
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "REGISTER"
  }'
```

**Propósitos válidos:**
- `REGISTER` - Para registro de usuarios
- `LOGIN` - Para login de usuarios
- `RESET_PASSWORD` - Para recuperación de contraseña
- `EMAIL_CONFIRMATION` - Para confirmación de email

**Request Body:**
```json
{
  "email": "usuario@example.com",
  "purpose": "REGISTER"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "OTP generado correctamente",
  "expires_in_minutes": 5
}
```

**Errores posibles:**
- `400 Bad Request` - Email inválido o propósito incorrecto
- `500 Internal Server Error` - Error interno del servidor

---

### 3. Validar OTP

**Endpoint:** `POST /api/auth/verify`

**Descripción:** Valida un código OTP. Si el propósito es `EMAIL_CONFIRMATION` y el OTP es válido, marca el email como confirmado automáticamente.

**Request:**
```bash
curl -X POST "http://localhost:8082/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "otp": "123456",
    "purpose": "REGISTER"
  }'
```

**Request Body:**
```json
{
  "email": "usuario@example.com",
  "otp": "123456",
  "purpose": "REGISTER"
}
```

**Response (200 OK - OTP válido):**
```json
{
  "success": true,
  "message": "OTP válido",
  "valid": true
}
```

**Errores posibles:**
- `400 Bad Request` - OTP inválido
- `410 Gone` - OTP expirado
- `429 Too Many Requests` - Máximo de intentos alcanzado
- `500 Internal Server Error` - Error interno del servidor

---

### 4. Verificar Estado de Confirmación de Email

**Endpoint:** `GET /api/auth/verify-email-status?email={email}`

**Descripción:** Verifica si un email está confirmado. Retorna solo el estado (confirmed: true/false).

**Request:**
```bash
curl -X GET "http://localhost:8082/api/auth/verify-email-status?email=usuario@example.com"
```

**Query Parameters:**
- `email` (requerido) - Email del usuario a verificar

**Response (200 OK):**
```json
{
  "confirmed": true,
  "email": "usuario@example.com"
}
```

**Response (si no está confirmado):**
```json
{
  "confirmed": false,
  "email": "usuario@example.com"
}
```

**Errores posibles:**
- `400 Bad Request` - Email inválido
- `500 Internal Server Error` - Error interno del servidor

---

### 5. Confirmar Email (Verificar Estado)

**Endpoint:** `GET /api/auth/confirm-email?email={email}`

**Descripción:** Verifica si un email está confirmado y retorna un mensaje descriptivo.

**Request:**
```bash
curl -X GET "http://localhost:8082/api/auth/confirm-email?email=usuario@example.com"
```

**Query Parameters:**
- `email` (requerido) - Email del usuario a verificar

**Response (200 OK - Email confirmado):**
```json
{
  "email": "usuario@example.com",
  "confirmed": true,
  "message": "Email confirmado exitosamente"
}
```

**Response (200 OK - Email no confirmado):**
```json
{
  "email": "usuario@example.com",
  "confirmed": false,
  "message": "El email aún no ha sido confirmado (OTP no verificado)"
}
```

**Errores posibles:**
- `400 Bad Request` - Email inválido
- `500 Internal Server Error` - Error interno del servidor

---

## 🔄 Ejemplos de Flujos Completos

### Ejemplo 1: Flujo de Registro

```bash
# Paso 1: Generar OTP para registro
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@example.com",
    "purpose": "REGISTER"
  }'

# Paso 2: Validar OTP (usuario ingresa el código recibido por email)
curl -X POST "http://localhost:8082/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@example.com",
    "otp": "123456",
    "purpose": "REGISTER"
  }'
```

### Ejemplo 2: Flujo de Confirmación de Email

```bash
# Paso 1: Generar OTP para confirmación de email
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "EMAIL_CONFIRMATION"
  }'

# Paso 2: Validar OTP (esto marca el email como confirmado)
curl -X POST "http://localhost:8082/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "otp": "123456",
    "purpose": "EMAIL_CONFIRMATION"
  }'

# Paso 3: Verificar estado de confirmación
curl -X GET "http://localhost:8082/api/auth/verify-email-status?email=usuario@example.com"

# O alternativamente:
curl -X GET "http://localhost:8082/api/auth/confirm-email?email=usuario@example.com"
```

### Ejemplo 3: Flujo de Login

```bash
# Paso 1: Generar OTP para login
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "LOGIN"
  }'

# Paso 2: Validar OTP
curl -X POST "http://localhost:8082/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "otp": "123456",
    "purpose": "LOGIN"
  }'
```

### Ejemplo 4: Flujo de Recuperación de Contraseña

```bash
# Paso 1: Generar OTP para reset de contraseña
curl -X POST "http://localhost:8082/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "RESET_PASSWORD"
  }'

# Paso 2: Validar OTP
curl -X POST "http://localhost:8082/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "otp": "123456",
    "purpose": "RESET_PASSWORD"
  }'
```

---

## 🔗 Integración con Servicio Java

El servicio Java (goat-listing) debe consumir estos endpoints de la siguiente manera:

### Desde Java (ejemplo conceptual):

```java
// Generar OTP para registro
POST http://localhost:8082/api/auth/otp
{
  "email": "user@example.com",
  "purpose": "REGISTER"
}

// Validar OTP
POST http://localhost:8082/api/auth/verify
{
  "email": "user@example.com",
  "otp": "123456",
  "purpose": "REGISTER"
}

// Verificar estado de confirmación de email
GET http://localhost:8082/api/auth/verify-email-status?email=user@example.com

// Confirmar email (verificar estado)
GET http://localhost:8082/api/auth/confirm-email?email=user@example.com
```

---

## ⚙️ Configuración de OTPs

**Configurable en `.env`:**

- `OTP_EXPIRATION_MINUTES=5` - Tiempo de expiración del OTP (default: 5 minutos)
- `OTP_MAX_ATTEMPTS=3` - Máximo de intentos de validación (default: 3 intentos)
- `OTP_LENGTH=6` - Longitud del código OTP (default: 6 dígitos)

---

## 🔒 Seguridad

- ✅ OTPs se almacenan como **hash** (nunca en texto plano)
- ✅ OTPs expiran automáticamente mediante **TTL en MongoDB**
- ✅ **Máximo de intentos** configurables para prevenir fuerza bruta
- ✅ Validación ofuscada para evitar enumeración de emails
- ✅ Rate limiting (extensible)

---

## 📊 Códigos de Estado HTTP

| Código | Significado | Uso |
|--------|-------------|-----|
| `200 OK` | Operación exitosa | Validar OTP, verificar estado |
| `201 Created` | Recurso creado | Generar OTP |
| `400 Bad Request` | Solicitud inválida | Email/OTP inválido, datos incorrectos |
| `410 Gone` | Recurso no disponible | OTP expirado |
| `429 Too Many Requests` | Demasiadas solicitudes | Máximo de intentos alcanzado |
| `500 Internal Server Error` | Error del servidor | Error interno |

---

## 💡 Notas Importantes

1. **OTPs no se muestran en respuestas**: Por seguridad, los OTPs nunca se retornan en las respuestas. Solo se envían por email.

2. **Confirmación automática**: Cuando se valida un OTP con propósito `EMAIL_CONFIRMATION`, el email se marca automáticamente como confirmado en la colección `confirmed_emails`.

3. **Eliminación automática**: Los OTPs expirados se eliminan automáticamente mediante TTL de MongoDB (después de 5 minutos por defecto).

4. **Unicidad de OTPs**: Un usuario puede tener un solo OTP activo por propósito. Si se genera un nuevo OTP, el anterior se elimina automáticamente.

5. **Emails confirmados**: Los emails confirmados se almacenan permanentemente en la colección `confirmed_emails` hasta que se eliminen manualmente.

---

## 🚀 Próximos Pasos

Para probar los endpoints:

1. Iniciar el servidor:
```bash
python start_server.py
```

2. Probar los endpoints usando los ejemplos de curl proporcionados arriba.

3. Acceder a la documentación interactiva en: `http://localhost:8082/docs`


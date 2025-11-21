# 📚 Guía Completa de Integración - GOAT Authentication & OTP Service

## 🎯 Visión General

Esta guía proporciona toda la información necesaria para integrar el frontend con los servicios de autenticación y OTP. El sistema está compuesto por dos servicios backend:

- **Servicio Java (Identity)**: `http://localhost:8081` - Gestión de usuarios, autenticación y autorización
- **Servicio Python (Catalog & Auth)**: `http://localhost:8082` - Gestión de OTPs y confirmación de emails

---

## 🔗 Arquitectura de Servicios

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│   Frontend  │ ───────> │ Servicio Java│ ───────> │ Servicio     │
│             │         │ (Puerto 8081) │         │ Python       │
│             │         │              │         │ (Puerto 8082) │
└─────────────┘         └──────────────┘         └──────────────┘
```

**Flujo de comunicación:**
- El frontend **solo se comunica** con el servicio Java (puerto 8081)
- El servicio Java se comunica internamente con el servicio Python cuando es necesario
- El frontend **NO debe** llamar directamente al servicio Python

### 🔐 Responsabilidades de Cada Servicio

#### Servicio Java (Puerto 8081)
- ✅ Gestión de usuarios (crear, buscar, actualizar)
- ✅ Autenticación y autorización (login, tokens JWT)
- ✅ Validación de reglas de negocio (email confirmado, usuario activo)
- ✅ **Intermediario/Proxy** para operaciones de OTP
- ✅ Actualización de estado en PostgreSQL cuando corresponde

#### Servicio Python (Puerto 8082)
- ✅ **Generación de códigos OTP** (única fuente de generación)
- ✅ **Almacenamiento de OTPs en MongoDB** (con TTL automático)
- ✅ **Envío de OTPs por email**
- ✅ **Validación de OTPs** (verifica expiración, intentos, etc.)
- ✅ Gestión de emails confirmados en MongoDB

---

## 📋 Endpoints del Servicio Java (Frontend → Java)

### Base URL
```
http://localhost:8081
```

### Headers Comunes
```http
Content-Type: application/json
Accept: application/json
```

Para endpoints protegidos (requieren autenticación):
```http
Authorization: Bearer {JWT_TOKEN}
```

---

## 🔐 Flujos de Autenticación Completos

### 1. 🔑 Flujo de Registro de Usuario

**Descripción:** Permite crear una nueva cuenta de usuario con verificación de email mediante OTP.

#### Paso 1: Crear Usuario
**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "email": "nuevo@example.com",
  "password": "miPassword123"
}
```

**Validaciones:**
- `email`: Requerido, formato de email válido
- `password`: Requerido, mínimo 8 caracteres

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "nuevo@example.com",
  "emailConfirmed": false,
  "isActive": true
}
```

**Errores posibles:**
- `400 Bad Request`: Email o contraseña inválidos
- `409 Conflict`: El email ya existe en el sistema
- `500 Internal Server Error`: Error interno del servidor

**Ejemplo con cURL:**
```bash
curl -X POST "http://localhost:8081/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@example.com",
    "password": "miPassword123"
  }'
```

#### Paso 2: Generar OTP para Confirmación de Email
**Endpoint:** `POST /api/auth/otp`

**Request:**
```json
{
  "email": "nuevo@example.com",
  "purpose": "EMAIL_CONFIRMATION"
}
```

**Propósitos válidos:**
- `REGISTER` - Para registro de usuarios
- `LOGIN` - Para login de usuarios
- `EMAIL_CONFIRMATION` - Para confirmación de email
- `RESET_PASSWORD` - Para recuperación de contraseña

**Response (201 Created):**
```json
{
  "success": true,
  "message": "OTP generado correctamente",
  "expiresInMinutes": 5
}
```

**Errores posibles:**
- `400 Bad Request`: Email inválido o propósito incorrecto
- `500 Internal Server Error`: Error al generar OTP (servicio Python no disponible)

**⚠️ Importante - Flujo Interno:**
1. El frontend llama a `POST /api/auth/otp` en el servicio Java
2. El servicio Java **NO genera el OTP**, solo actúa como intermediario
3. Java reenvía la petición al servicio Python: `POST http://localhost:8082/api/auth/otp`
4. **El servicio Python es quien:**
   - Genera el código OTP (6 dígitos aleatorios)
   - Guarda el OTP hasheado en MongoDB con TTL (expira en 5 minutos)
   - Envía el código OTP por email al usuario
5. Python retorna éxito al servicio Java
6. Java retorna la respuesta al frontend

**El código OTP nunca se genera en Java, solo en Python.**

**Ejemplo con cURL:**
```bash
curl -X POST "http://localhost:8081/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@example.com",
    "purpose": "EMAIL_CONFIRMATION"
  }'
```

#### Paso 3: Verificar OTP
**Endpoint:** `POST /api/auth/verify`

**Request:**
```json
{
  "email": "nuevo@example.com",
  "otp": "123456",
  "purpose": "EMAIL_CONFIRMATION"
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

**Response (400 Bad Request - OTP inválido):**
```json
{
  "success": false,
  "message": "OTP inválido o expirado",
  "valid": false
}
```

**Errores posibles:**
- `400 Bad Request`: OTP inválido o expirado
- `500 Internal Server Error`: Error interno del servidor

**⚠️ Importante - Flujo Interno de Verificación:**
1. El frontend llama a `POST /api/auth/verify` en el servicio Java
2. El servicio Java **NO valida el OTP**, solo actúa como intermediario
3. Java reenvía la petición al servicio Python: `POST http://localhost:8082/api/auth/verify`
4. **El servicio Python es quien:**
   - Busca el OTP en MongoDB
   - Verifica que el código coincida (comparando hash)
   - Verifica que no haya expirado (TTL)
   - Verifica que no se hayan excedido los intentos máximos
   - Retorna si el OTP es válido o no
5. Si el OTP es válido y `purpose = "EMAIL_CONFIRMATION"`:
   - **Java actualiza `email_confirmed = true` en PostgreSQL** (tabla `identity.users`)
   - Esta actualización se hace automáticamente, no requiere endpoint adicional
6. Java retorna la respuesta al frontend

**Resumen:**
- ✅ **Validación del OTP**: Se hace en Python (MongoDB)
- ✅ **Actualización de estado**: Se hace en Java (PostgreSQL) cuando corresponde
- ✅ **El OTP nunca se valida en Java**, solo se delega a Python

**Ejemplo con cURL:**
```bash
curl -X POST "http://localhost:8081/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@example.com",
    "otp": "123456",
    "purpose": "EMAIL_CONFIRMATION"
  }'
```

#### Paso 4: Verificar Estado de Confirmación (Opcional)
**Endpoint:** `GET /api/auth/confirm-email?email={email}`

**Request:**
```bash
GET /api/auth/confirm-email?email=nuevo@example.com
```

**Response (200 OK - Email confirmado):**
```json
{
  "email": "nuevo@example.com",
  "confirmed": true,
  "message": "Email confirmado exitosamente"
}
```

**Response (200 OK - Email no confirmado):**
```json
{
  "email": "nuevo@example.com",
  "confirmed": false,
  "message": "El email aún no ha sido confirmado (OTP no verificado)"
}
```

---

### 2. 🔐 Flujo de Login de Usuario

**Descripción:** Autentica un usuario existente. Requiere que el email esté confirmado.

#### Opción A: Login Tradicional (Email + Password)

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "email": "usuario@example.com",
  "password": "miPassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "usuario@example.com",
  "roles": ["BUYER", "CLIENT"],
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Errores posibles:**
- `400 Bad Request`: Email o contraseña inválidos
- `401 Unauthorized`: Credenciales inválidas
- `403 Forbidden`: Usuario inactivo
- `403 Forbidden`: Email no confirmado

**Ejemplo con cURL:**
```bash
curl -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "miPassword123"
  }'
```

#### Opción B: Login con OTP (Sin contraseña)

**Paso 1: Generar OTP para Login**
**Endpoint:** `POST /api/auth/otp`

**Request:**
```json
{
  "email": "usuario@example.com",
  "purpose": "LOGIN"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "OTP generado correctamente",
  "expiresInMinutes": 5
}
```

**Paso 2: Verificar OTP**
**Endpoint:** `POST /api/auth/verify`

**Request:**
```json
{
  "email": "usuario@example.com",
  "otp": "123456",
  "purpose": "LOGIN"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "OTP válido",
  "valid": true
}
```

**Nota:** Después de verificar el OTP, el frontend debe realizar un login tradicional o el backend debe generar un token JWT automáticamente (depende de la implementación específica).

---

### 3. 📧 Flujo de Confirmación de Email (Post-Registro)

**Descripción:** Permite confirmar el email de un usuario que ya se registró pero no ha confirmado su email.

#### Paso 1: Generar OTP
**Endpoint:** `POST /api/auth/otp`

**Request:**
```json
{
  "email": "usuario@example.com",
  "purpose": "EMAIL_CONFIRMATION"
}
```

#### Paso 2: Verificar OTP
**Endpoint:** `POST /api/auth/verify`

**Request:**
```json
{
  "email": "usuario@example.com",
  "otp": "123456",
  "purpose": "EMAIL_CONFIRMATION"
}
```

**Nota:** Al verificar el OTP con propósito `EMAIL_CONFIRMATION`, el sistema automáticamente actualiza el campo `email_confirmed` del usuario a `true`.

#### Paso 3: Verificar Estado
**Endpoint:** `GET /api/auth/confirm-email?email={email}`

---

### 4. 🔄 Flujo de Recuperación de Contraseña

**Descripción:** Permite a un usuario recuperar acceso a su cuenta mediante OTP.

#### Paso 1: Generar OTP para Reset
**Endpoint:** `POST /api/auth/otp`

**Request:**
```json
{
  "email": "usuario@example.com",
  "purpose": "RESET_PASSWORD"
}
```

#### Paso 2: Verificar OTP
**Endpoint:** `POST /api/auth/verify`

**Request:**
```json
{
  "email": "usuario@example.com",
  "otp": "123456",
  "purpose": "RESET_PASSWORD"
}
```

**Nota:** Después de verificar el OTP, el frontend debe mostrar un formulario para ingresar la nueva contraseña. El endpoint para actualizar la contraseña debe implementarse según los requerimientos específicos.

---

## 📊 Diagramas de Flujo

### Flujo Completo de Registro

```
┌──────────┐
│ Frontend│
└────┬─────┘
     │
     │ 1. POST /api/auth/register
     │    {email, password}
     ▼
┌─────────────────┐
│ Servicio Java   │
│ (Puerto 8081)   │
└────┬────────────┘
     │
     │ 2. Crea usuario en PostgreSQL
     │    email_confirmed = false
     │
     │ 3. Retorna usuario creado
     ▼
┌──────────┐
│ Frontend │
└────┬─────┘
     │
     │ 4. POST /api/auth/otp
     │    {email, purpose: "EMAIL_CONFIRMATION"}
     ▼
┌─────────────────┐         ┌──────────────────┐
│ Servicio Java   │────────>│ Servicio Python  │
│ (Puerto 8081)   │         │ (Puerto 8082)    │
│ [PROXY]         │         │                  │
└─────────────────┘         └──────────────────┘
     │                            │
     │                            │ 5. Python genera código OTP
     │                            │    (6 dígitos aleatorios)
     │                            │
     │                            │ 6. Python guarda OTP hasheado
     │                            │    en MongoDB con TTL (5 min)
     │                            │
     │                            │ 7. Python envía OTP por email
     │                            │
     │ 8. Python retorna success  │
     │    Java retorna al frontend │
     ▼                            │
┌──────────┐                     │
│ Frontend │                     │
└────┬─────┘                     │
     │                            │
     │ 9. Usuario ingresa OTP     │
     │    recibido por email      │
     │                            │
     │ 10. POST /api/auth/verify  │
     │     {email, otp, purpose}  │
     ▼                            │
┌─────────────────┐         ┌──────────────────┐
│ Servicio Java   │────────>│ Servicio Python  │
│ (Puerto 8081)   │         │ (Puerto 8082)    │
│ [PROXY]         │         │                  │
└─────────────────┘         └──────────────────┘
     │                            │
     │                            │ 11. Python busca OTP en MongoDB
     │                            │
     │                            │ 12. Python valida:
     │                            │     - Código coincide (hash)
     │                            │     - No ha expirado (TTL)
     │                            │     - Intentos no excedidos
     │                            │
     │                            │ 13. Python retorna: valid = true
     │                            │
     │ 14. Si purpose = EMAIL_CONFIRMATION │
     │     Java actualiza PostgreSQL:      │
     │     UPDATE users SET email_confirmed = true │
     │                            │
     │ 15. Java retorna valid: true │
     ▼                            │
┌──────────┐                     │
│ Frontend │                     │
└──────────┘                     │
```

**Leyenda:**
- 🔵 **Azul**: Operaciones del Servicio Java
- 🟢 **Verde**: Operaciones del Servicio Python
- ⚪ **Blanco**: Interacción con Frontend

---

## 🔧 Manejo de Errores

### Estructura de Respuesta de Error

Todos los errores siguen esta estructura:

```json
{
  "timestamp": "2025-01-20T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción detallada del error"
}
```

### Códigos de Estado HTTP

| Código | Significado | Uso Común |
|--------|-------------|-----------|
| `200 OK` | Operación exitosa | Login exitoso, verificación de OTP |
| `201 Created` | Recurso creado | Registro de usuario, generación de OTP |
| `400 Bad Request` | Solicitud inválida | Datos faltantes o inválidos |
| `401 Unauthorized` | No autenticado | Credenciales inválidas |
| `403 Forbidden` | No autorizado | Usuario inactivo, email no confirmado |
| `409 Conflict` | Conflicto | Email ya existe |
| `500 Internal Server Error` | Error del servidor | Error interno |

### Errores Específicos por Endpoint

#### POST /api/auth/register

**409 Conflict - Email ya existe:**
```json
{
  "timestamp": "2025-01-20T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "El email ya existe en el sistema"
}
```

#### POST /api/auth/login

**401 Unauthorized - Credenciales inválidas:**
```json
{
  "timestamp": "2025-01-20T10:30:00",
  "status": 401,
  "error": "Credenciales inválidas",
  "message": "Credenciales inválidas"
}
```

**403 Forbidden - Usuario inactivo:**
```json
{
  "timestamp": "2025-01-20T10:30:00",
  "status": 403,
  "error": "Usuario inactivo",
  "message": "El usuario está inactivo"
}
```

**403 Forbidden - Email no confirmado:**
```json
{
  "timestamp": "2025-01-20T10:30:00",
  "status": 403,
  "error": "Email no confirmado",
  "message": "El email no ha sido confirmado"
}
```

#### POST /api/auth/otp

**500 Internal Server Error - Servicio Python no disponible:**
```json
{
  "success": false,
  "message": "Error al generar el OTP. Verifique que el servicio Python esté disponible.",
  "expiresInMinutes": null
}
```

#### POST /api/auth/verify

**400 Bad Request - OTP inválido:**
```json
{
  "success": false,
  "message": "OTP inválido o expirado",
  "valid": false
}
```

---

## 💻 Ejemplos de Código para Frontend

### JavaScript/TypeScript (Fetch API)

#### Función de Registro Completo

```typescript
interface RegisterRequest {
  email: string;
  password: string;
}

interface RegisterResponse {
  id: string;
  email: string;
  emailConfirmed: boolean;
  isActive: boolean;
}

interface GenerateOtpRequest {
  email: string;
  purpose: 'REGISTER' | 'LOGIN' | 'EMAIL_CONFIRMATION' | 'RESET_PASSWORD';
}

interface GenerateOtpResponse {
  success: boolean;
  message: string;
  expiresInMinutes: number | null;
}

interface VerifyOtpRequest {
  email: string;
  otp: string;
  purpose: string;
}

interface VerifyOtpResponse {
  success: boolean;
  message: string;
  valid: boolean;
}

const API_BASE_URL = 'http://localhost:8081';

/**
 * Registra un nuevo usuario y confirma su email con OTP
 */
async function registerUserWithEmailConfirmation(
  email: string,
  password: string
): Promise<{ success: boolean; message: string }> {
  try {
    // Paso 1: Crear usuario
    const registerResponse = await fetch(`${API_BASE_URL}/api/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    if (!registerResponse.ok) {
      if (registerResponse.status === 409) {
        return { success: false, message: 'El email ya existe' };
      }
      const error = await registerResponse.json();
      return { success: false, message: error.message || 'Error al registrar usuario' };
    }

    const user: RegisterResponse = await registerResponse.json();

    // Paso 2: Generar OTP
    const otpResponse = await fetch(`${API_BASE_URL}/api/auth/otp`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email,
        purpose: 'EMAIL_CONFIRMATION',
      }),
    });

    if (!otpResponse.ok) {
      return { success: false, message: 'Error al generar código OTP' };
    }

    const otpData: GenerateOtpResponse = await otpResponse.json();
    
    if (!otpData.success) {
      return { success: false, message: otpData.message };
    }

    // Retornar éxito - el usuario debe ingresar el OTP recibido por email
    return {
      success: true,
      message: `Código OTP enviado a ${email}. Por favor, ingrésalo para confirmar tu email.`,
    };
  } catch (error) {
    return { success: false, message: 'Error de conexión con el servidor' };
  }
}

/**
 * Verifica el OTP ingresado por el usuario
 */
async function verifyOtp(
  email: string,
  otp: string,
  purpose: string
): Promise<{ success: boolean; message: string }> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/verify`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, otp, purpose }),
    });

    const data: VerifyOtpResponse = await response.json();

    if (data.valid) {
      return { success: true, message: data.message };
    } else {
      return { success: false, message: data.message };
    }
  } catch (error) {
    return { success: false, message: 'Error de conexión con el servidor' };
  }
}

/**
 * Login tradicional con email y contraseña
 */
async function login(
  email: string,
  password: string
): Promise<{ success: boolean; token?: string; message: string }> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      const error = await response.json();
      
      if (response.status === 401) {
        return { success: false, message: 'Credenciales inválidas' };
      }
      if (response.status === 403) {
        return { success: false, message: error.message || 'Usuario inactivo o email no confirmado' };
      }
      
      return { success: false, message: error.message || 'Error al iniciar sesión' };
    }

    const data = await response.json();
    
    // Guardar token en localStorage o en el estado de la aplicación
    localStorage.setItem('authToken', data.token);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('userId', data.userId);
    
    return { success: true, token: data.token, message: 'Login exitoso' };
  } catch (error) {
    return { success: false, message: 'Error de conexión con el servidor' };
  }
}

/**
 * Verifica el estado de confirmación de email
 */
async function checkEmailConfirmation(
  email: string
): Promise<{ confirmed: boolean; message: string }> {
  try {
    const response = await fetch(
      `${API_BASE_URL}/api/auth/confirm-email?email=${encodeURIComponent(email)}`
    );

    if (!response.ok) {
      return { confirmed: false, message: 'Error al verificar el estado del email' };
    }

    const data = await response.json();
    return { confirmed: data.confirmed, message: data.message };
  } catch (error) {
    return { confirmed: false, message: 'Error de conexión con el servidor' };
  }
}

// Ejemplo de uso en un componente React
function RegisterComponent() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [step, setStep] = useState<'register' | 'verify'>('register');
  const [message, setMessage] = useState('');

  const handleRegister = async () => {
    const result = await registerUserWithEmailConfirmation(email, password);
    setMessage(result.message);
    if (result.success) {
      setStep('verify');
    }
  };

  const handleVerifyOtp = async () => {
    const result = await verifyOtp(email, otp, 'EMAIL_CONFIRMATION');
    setMessage(result.message);
    if (result.success) {
      // Redirigir a login o dashboard
      window.location.href = '/login';
    }
  };

  return (
    <div>
      {step === 'register' ? (
        <div>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Email"
          />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Contraseña"
          />
          <button onClick={handleRegister}>Registrarse</button>
          {message && <p>{message}</p>}
        </div>
      ) : (
        <div>
          <p>Ingresa el código OTP enviado a {email}</p>
          <input
            type="text"
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
            placeholder="Código OTP"
          />
          <button onClick={handleVerifyOtp}>Verificar</button>
          {message && <p>{message}</p>}
        </div>
      )}
    </div>
  );
}
```

---

## 🔐 Manejo de Tokens JWT

### Almacenamiento del Token

Después de un login exitoso, el frontend recibe un token JWT que debe almacenarse y enviarse en las peticiones subsiguientes.

**Recomendaciones:**
- **localStorage**: Para aplicaciones web simples (menos seguro, vulnerable a XSS)
- **httpOnly cookies**: Más seguro, pero requiere configuración adicional
- **sessionStorage**: Se elimina al cerrar la pestaña

### Envío del Token en Peticiones

```typescript
async function makeAuthenticatedRequest(url: string, options: RequestInit = {}) {
  const token = localStorage.getItem('authToken');
  
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
    ...options.headers,
  };

  return fetch(url, {
    ...options,
    headers,
  });
}

// Ejemplo de uso
const response = await makeAuthenticatedRequest(`${API_BASE_URL}/api/protected-endpoint`, {
  method: 'GET',
});
```

### Decodificación del Token (Opcional)

Si necesitas leer información del token sin validarlo:

```typescript
function decodeJwt(token: string) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    return null;
  }
}

// Ejemplo de uso
const token = localStorage.getItem('authToken');
if (token) {
  const payload = decodeJwt(token);
  console.log('User ID:', payload.sub);
  console.log('Email:', payload.email);
  console.log('Roles:', payload.roles);
  console.log('Expires:', new Date(payload.exp * 1000));
}
```

---

## 📝 Checklist de Implementación Frontend

### Registro de Usuario
- [ ] Formulario de registro con validación de email y contraseña
- [ ] Llamada a `POST /api/auth/register`
- [ ] Manejo de error 409 (email ya existe)
- [ ] Llamada a `POST /api/auth/otp` con purpose `EMAIL_CONFIRMATION`
- [ ] Pantalla para ingresar OTP
- [ ] Llamada a `POST /api/auth/verify`
- [ ] Manejo de OTP inválido/expirado
- [ ] Redirección a login después de confirmación exitosa

### Login
- [ ] Formulario de login con email y contraseña
- [ ] Llamada a `POST /api/auth/login`
- [ ] Manejo de errores (401, 403)
- [ ] Almacenamiento del token JWT
- [ ] Redirección a dashboard/home después de login exitoso

### Login con OTP
- [ ] Opción para login con OTP
- [ ] Llamada a `POST /api/auth/otp` con purpose `LOGIN`
- [ ] Pantalla para ingresar OTP
- [ ] Llamada a `POST /api/auth/verify`
- [ ] Generación/obtención de token después de verificación

### Recuperación de Contraseña
- [ ] Formulario para solicitar reset
- [ ] Llamada a `POST /api/auth/otp` con purpose `RESET_PASSWORD`
- [ ] Pantalla para ingresar OTP
- [ ] Llamada a `POST /api/auth/verify`
- [ ] Formulario para nueva contraseña (endpoint a implementar)

### Confirmación de Email
- [ ] Verificación de estado de email después de registro
- [ ] Llamada a `GET /api/auth/confirm-email?email={email}`
- [ ] Opción para reenviar OTP si no está confirmado

---

## 🚨 Consideraciones de Seguridad

1. **Nunca almacenes contraseñas en texto plano** - El backend ya las hashea con BCrypt
2. **Valida emails en el frontend** - Pero nunca confíes solo en validación del frontend
3. **Maneja tokens de forma segura** - Considera usar httpOnly cookies en producción
4. **Implementa rate limiting en el frontend** - Previene spam de peticiones
5. **Muestra mensajes de error genéricos** - No expongas información sensible
6. **Implementa timeout para OTPs** - Muestra cuenta regresiva de 5 minutos
7. **Valida formato de OTP** - Solo números, 6 dígitos

## 🔍 Detalles Técnicos del Flujo de OTP

### ¿Dónde se genera el OTP?
- ✅ **Solo en el Servicio Python** (Puerto 8082)
- ❌ **NO se genera en Java**
- El servicio Java actúa como **proxy/intermediario** entre el frontend y Python

### ¿Dónde se valida el OTP?
- ✅ **Solo en el Servicio Python** (Puerto 8082)
- ❌ **NO se valida en Java**
- Python verifica en MongoDB: código, expiración, intentos máximos
- El servicio Java solo reenvía la petición y procesa el resultado

### ¿Dónde se almacena el OTP?
- ✅ **MongoDB** (base de datos `auth` en el servicio Python)
- ❌ **NO se almacena en PostgreSQL**
- Los OTPs tienen TTL (Time To Live) de 5 minutos y se eliminan automáticamente

### ¿Dónde se actualiza el estado de email confirmado?
- ✅ **PostgreSQL** (tabla `identity.users`, campo `email_confirmed`)
- ✅ **Se actualiza automáticamente** cuando se verifica un OTP con `purpose: "EMAIL_CONFIRMATION"`
- ✅ **Se actualiza desde el Servicio Java** después de recibir confirmación de Python

### Flujo Detallado de Generación de OTP

```
Frontend → Java (POST /api/auth/otp)
    ↓
Java valida request (email, purpose)
    ↓
Java → Python (POST http://localhost:8082/api/auth/otp)
    ↓
Python genera código OTP (6 dígitos aleatorios)
    ↓
Python hashea el OTP (nunca se guarda en texto plano)
    ↓
Python guarda hash en MongoDB con TTL de 5 minutos
    ↓
Python envía OTP por email al usuario
    ↓
Python retorna { success: true } a Java
    ↓
Java retorna respuesta al Frontend
```

### Flujo Detallado de Verificación de OTP

```
Frontend → Java (POST /api/auth/verify)
    ↓
Java valida request (email, otp, purpose)
    ↓
Java → Python (POST http://localhost:8082/api/auth/verify)
    ↓
Python busca OTP en MongoDB por email y purpose
    ↓
Python hashea el OTP recibido
    ↓
Python compara hash recibido vs hash almacenado
    ↓
Python verifica:
  - Hash coincide ✅
  - No ha expirado (TTL) ✅
  - Intentos no excedidos ✅
    ↓
Python retorna { valid: true/false } a Java
    ↓
Si valid = true Y purpose = "EMAIL_CONFIRMATION":
    Java actualiza PostgreSQL:
    UPDATE identity.users 
    SET email_confirmed = true 
    WHERE email = ?
    ↓
Java retorna respuesta al Frontend
```

---

## 📚 Referencias

- **Servicio Java (Identity)**: `http://localhost:8081`
- **Servicio Python (Catalog & Auth)**: `http://localhost:8082`
- **Documentación Python Service**: Ver `API_DOCUMENTATION.md` (documento original)

---

## 🆘 Soporte

Para problemas o preguntas sobre la integración:
1. Verifica que ambos servicios estén corriendo
2. Revisa los logs del servidor Java
3. Verifica la conectividad con el servicio Python
4. Consulta la documentación del servicio Python para detalles de OTP

---

**Última actualización:** 2025-01-20


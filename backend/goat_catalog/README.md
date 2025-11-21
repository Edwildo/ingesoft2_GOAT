# GOAT Catalog & Authentication Service

Microservicio Python para gestión de catálogo canónico y tokens de autenticación efímeros.

## 🏗️ Arquitectura

- **Domain-Driven Design (DDD)**: Bounded contexts claros (Catalog, Tokens)
- **Clean Architecture**: Separación por capas (Domain, Application, Infrastructure, Entrypoints)
- **Screaming Architecture**: Estructura refleja el dominio del negocio

## 📁 Estructura del Proyecto

```
src/goat_catalog/
├── catalog/           # Bounded Context: Catalog
│   ├── sneakers/      # Aggregate: Sneakers
│   ├── categories/    # Aggregate: Categories
│   ├── brands/        # Aggregate: Brands
│   └── collections/   # Aggregate: Collections
├── tokens/            # Bounded Context: Tokens
│   └── otps/          # Aggregate: OTPs
├── shared/            # Shared Kernel
└── bootstrap/         # Dependency Injection & App Factory
```

## 🚀 Inicio Rápido

### Prerrequisitos

- Python 3.11+
- Poetry (gestor de dependencias)
- Docker y Docker Compose (para MongoDB)

### Instalación

1. Clonar el repositorio
2. Instalar dependencias:
```bash
poetry install
```

3. Configurar variables de entorno:
```bash
# Crear archivo .env
touch .env
```

Editar el archivo `.env` con tus configuraciones:

**Para MongoDB Local (Docker):**
```env
MONGODB_URI=mongodb://localhost:27017
MONGODB_DATABASE=auth
```

**Para MongoDB Atlas (Cloud):**
```env
MONGODB_URI=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
MONGODB_DATABASE=auth
```

**Ejemplo de URI de Atlas:**
```env
MONGODB_URI=mongodb+srv://epinillap:TuPassword@cluster0.ay2yam4.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0
MONGODB_DATABASE=auth
```

**Configuración SMTP (Email):**
```env
# Configuración básica
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu-email@gmail.com
SMTP_PASSWORD=tu-app-password
EMAIL_FROM=noreply@goat.com

# Seguridad SSL/TLS (recomendado: true en producción)
SMTP_VALIDATE_CERT=true

# Configuración de retry para emails
EMAIL_MAX_RETRIES=3
EMAIL_RETRY_INITIAL_DELAY=1.0
EMAIL_RETRY_MAX_DELAY=10.0
```

**Proveedores SMTP comunes:**

- **Gmail**: `SMTP_HOST=smtp.gmail.com`, `SMTP_PORT=587`, requiere App Password
- **SendGrid**: `SMTP_HOST=smtp.sendgrid.net`, `SMTP_PORT=587`, usa API Key como password
- **Outlook/Hotmail**: `SMTP_HOST=smtp-mail.outlook.com`, `SMTP_PORT=587`
- **AWS SES**: `SMTP_HOST=email-smtp.region.amazonaws.com`, `SMTP_PORT=587`

**Nota de seguridad:** En producción, siempre usa `SMTP_VALIDATE_CERT=true` para prevenir ataques MITM. Solo desactívalo en desarrollo si es necesario.

**Troubleshooting SMTP común:**

- **Error de autenticación**: Verifica que `SMTP_USER` y `SMTP_PASSWORD` sean correctos. En Gmail, necesitas usar una App Password, no tu contraseña normal.
- **Error de conexión**: Verifica que `SMTP_HOST` y `SMTP_PORT` sean correctos para tu proveedor.
- **Error SSL/TLS**: Si tienes problemas con certificados en desarrollo, puedes temporalmente usar `SMTP_VALIDATE_CERT=false`, pero nunca en producción.
- **Timeout**: Aumenta los valores de retry (`EMAIL_MAX_RETRIES`, `EMAIL_RETRY_MAX_DELAY`) si tienes conexiones lentas.
- **Puerto incorrecto**: 
  - Puerto 587: Usa STARTTLS (recomendado)
  - Puerto 465: Usa TLS directo
  - Puerto 25: Generalmente bloqueado por ISPs

4. **Si usas MongoDB Local**, iniciar MongoDB:
```bash
docker-compose up -d
```

5. Inicializar índices MongoDB:
```bash
poetry run python scripts/init_mongo_indexes.py
```

6. Ejecutar el servidor:

**Opción 1: Usando el script de inicio (recomendado):**
```bash
python start_server.py
```

**Opción 2: Usando uvicorn directamente:**
```bash
# PowerShell
$env:PYTHONPATH="src"; python -m uvicorn goat_catalog.main:app --reload --port 8082

# Bash/Linux
PYTHONPATH=src python -m uvicorn goat_catalog.main:app --reload --port 8082
```

**Nota:** Si hay problemas de conexión a MongoDB Atlas, el servidor iniciará pero mostrará un warning. Verifica:
- Las credenciales en el archivo `.env`
- Que tu IP esté permitida en MongoDB Atlas (Network Access)

## 🔧 Desarrollo

### Estructura de un Agregado

Cada agregado sigue la estructura Clean Architecture:

```
aggregate_name/
├── domain/           # Reglas de negocio puras
│   ├── entities/     # Entidades del dominio
│   ├── value_objects/# Value Objects inmutables
│   ├── repositories/ # Interfaces (puertos)
│   ├── services/     # Domain Services
│   └── exceptions/   # Excepciones del dominio
├── application/      # Casos de uso
│   ├── use_cases/    # Casos de uso
│   └── dto/          # Data Transfer Objects
├── infrastructure/   # Implementaciones técnicas
│   ├── persistence/  # Repositorios MongoDB
│   └── models/       # Documentos MongoDB
└── entrypoints/      # Controllers FastAPI
    └── rest/
```

### Testing

```bash
# Ejecutar todos los tests
poetry run pytest

# Con cobertura
poetry run pytest --cov=src

# Tests específicos
poetry run pytest tests/unit/tokens/otps/
```

### Linting y Formateo

```bash
# Formatear código
poetry run black src/ tests/

# Linting
poetry run flake8 src/ tests/

# Type checking
poetry run mypy src/
```

## 📚 Documentación

- Ver `PYTHON_MONGO_ARCHITECTURE.md` para arquitectura detallada
- API docs disponibles en `/docs` cuando el servidor esté corriendo

## 🔐 Seguridad

- OTPs se almacenan como hash (nunca en texto plano)
- TTL automático en MongoDB para limpieza
- **Rate limiting** implementado:
  - Por IP: 10 requests/minuto para generación de OTPs
  - Por email: 5 OTPs/minuto por email
  - Por email: 10 verificaciones/minuto por email
  - Límite global: 60 requests/minuto por IP
- **Validación SSL/TLS** para conexiones SMTP:
  - Validación de certificados habilitada por defecto en producción
  - Configurable mediante `SMTP_VALIDATE_CERT` (false solo para desarrollo)
  - Previene ataques MITM (Man-in-the-Middle)
- Validación de inputs con Pydantic

## 🔗 Integración con Servicios Java

El servicio Java (goat-listing) se comunica con este servicio para:
- Generar OTPs: `POST /api/auth/otp`
- Validar OTPs: `POST /api/auth/verify`

## 📝 Notas

- Puerto por defecto: 8082
- MongoDB Local: puerto 27017
- MongoDB Atlas: Usa `mongodb+srv://` con ServerApi v1
- OTPs expiran después de 5 minutos (configurable)
- El proyecto soporta tanto MongoDB local como MongoDB Atlas


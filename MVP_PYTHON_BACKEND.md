# MVP - Python Backend (goat_catalog)

**Servicio:** OTP & Catalog Service  
**Puerto:** 8082  
**Base de Datos:** MongoDB (colecciones: `otps`, `confirmed_emails`, `catalog`)  
**Tecnología:** FastAPI, Motor (MongoDB), Pydantic

---

## 📋 Estado Actual

### ✅ Implementado:
- **OTP Service completo:**
  - Generación de OTPs (`POST /api/auth/otp`)
  - Validación de OTPs (`POST /api/auth/verify`)
  - Verificación de estado de email (`GET /api/auth/verify-email-status`)
  - Confirmación de email (`GET /api/auth/confirm-email`)
- **Infraestructura:**
  - Rate limiting por IP y email
  - Email service con SMTP (retry, SSL/TLS)
  - Health check (`/api/health`)
  - Inicialización automática de índices MongoDB
  - Logging sanitizado

### ❌ Pendiente para MVP:
- **Catalog Service completo:**
  - Sneakers (CRUD)
  - Brands (listado)
  - Categories (listado)
  - Collections (listado)
  - Validación de SKU para Java

---

## 🎯 Objetivos del MVP

1. **Catalog Service:** Gestionar catálogo canónico de sneakers, marcas, categorías y colecciones
2. **SKU Validation:** Proporcionar endpoint para que Java valide SKUs antes de crear listings
3. **Catalog API:** Exponer catálogo para frontend (a través de Java o directo)

---

## 📥 Entradas Esperadas (Dependencias)

### **De Java Backend (goat_listing):**

#### 1. OTP Requests (✅ Ya funcionando)
- **Endpoint consumido:** `POST /api/auth/otp`
- **Propósito:** Java solicita generación de OTP para usuarios
- **Input:** `{ "email": string, "purpose": string }`
- **Estado:** ✅ Funcionando correctamente

#### 2. OTP Verification Requests (✅ Ya funcionando)
- **Endpoint consumido:** `POST /api/auth/verify`
- **Propósito:** Java valida OTPs ingresados por usuarios
- **Input:** `{ "email": string, "otp": string, "purpose": string }`
- **Estado:** ✅ Funcionando correctamente

#### 3. Catalog Validation Requests (❌ Pendiente - CRÍTICO)
- **Endpoint a consumir:** `GET /api/catalog/sneakers/{sku}`
- **Propósito:** Java valida que SKU existe antes de crear listing
- **Input:** SKU (path parameter)
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Java Listings**
- **Prioridad:** 🔴 CRÍTICA

### **De Base de Datos:**

#### MongoDB - Colección `otps` (✅ Listo)
- Documentos con TTL para expiración automática
- Índices: `email`, `purpose`, `created_at`
- **Estado:** ✅ Funcionando

#### MongoDB - Colección `confirmed_emails` (✅ Listo)
- Documentos con email y timestamp de confirmación
- Índices: `email` (único)
- **Estado:** ✅ Funcionando

#### MongoDB - Colección `catalog` (❌ Pendiente)
- Subcolecciones necesarias:
  - `catalog.sneakers` - Sneakers canónicos
  - `catalog.brands` - Marcas
  - `catalog.categories` - Categorías
  - `catalog.collections` - Colecciones
- **Estado:** ❌ **NO IMPLEMENTADO - CRÍTICO**

---

## 📤 Salidas Esperadas (APIs y Resultados)

### **1. Catalog Service - Sneakers** 🔴 CRÍTICO

#### Endpoint: `GET /api/catalog/sneakers/{sku}`
- **Propósito:** Validar existencia de SKU (usado por Java)
- **Autenticación:** Público (o interno)
- **Path parameter:** `sku` (string)
- **Response esperado:**
  ```json
  {
    "sku": "string",
    "brand": "string",
    "model": "string",
    "gender": "string",
    "description": "string",
    "exists": true
  }
  ```
- **Códigos de respuesta:**
  - `200 OK`: SKU existe
  - `404 Not Found`: SKU no existe
- **Consumido por:** Java Backend (validación antes de crear listing)
- **Prioridad:** 🔴 CRÍTICA (bloquea Java Listings)

#### Endpoint: `GET /api/catalog/sneakers`
- **Propósito:** Buscar sneakers con filtros
- **Autenticación:** Público
- **Query params:** 
  - `?brand=string&category=string&gender=string&collection=string&search=string&page=number&size=number`
- **Response esperado:**
  ```json
  {
    "sneakers": [
      {
        "sku": "string",
        "brand": "string",
        "model": "string",
        "gender": "string",
        "description": "string",
        "categories": ["string"],
        "collections": ["string"],
        "media": {
          "coverImage": "string",
          "gallery": ["string"]
        }
      }
    ],
    "total": 0,
    "page": 0,
    "size": 20
  }
  ```
- **Consumido por:** Frontend (a través de Java o directo) para autocompletado y búsqueda
- **Prioridad:** 🔴 ALTA

#### Endpoint: `POST /api/catalog/sneakers`
- **Propósito:** Crear nuevo sneaker en catálogo
- **Autenticación:** Requerida (admin en futuro, por ahora público o interno)
- **Request body:**
  ```json
  {
    "sku": "string",
    "brand": "string",
    "model": "string",
    "gender": "string",
    "description": "string",
    "categories": ["string"],
    "collections": ["string"],
    "media": {
      "coverImage": "string",
      "gallery": ["string"]
    }
  }
  ```
- **Validaciones:**
  - SKU debe ser único
  - Brand debe existir
  - Categories deben existir
- **Response esperado:** Sneaker creado
- **Consumido por:** Scripts de inicialización, admin panel (futuro)
- **Prioridad:** 🟡 MEDIA

#### Endpoint: `PUT /api/catalog/sneakers/{sku}`
- **Propósito:** Actualizar información de sneaker
- **Autenticación:** Requerida (admin)
- **Request body:** Mismo formato que POST
- **Prioridad:** 🟡 MEDIA

---

### **2. Catalog Service - Brands** 🔴 CRÍTICO

#### Endpoint: `GET /api/catalog/brands`
- **Propósito:** Listar todas las marcas (para filtros en frontend)
- **Autenticación:** Público
- **Response esperado:**
  ```json
  {
    "brands": [
      {
        "id": "string",
        "name": "string",
        "slug": "string",
        "logoUrl": "string"
      }
    ]
  }
  ```
- **Consumido por:** Frontend (filtros en Shop y Create Listing)
- **Prioridad:** 🔴 ALTA

---

### **3. Catalog Service - Categories** 🔴 CRÍTICO

#### Endpoint: `GET /api/catalog/categories`
- **Propósito:** Listar todas las categorías
- **Autenticación:** Público
- **Response esperado:**
  ```json
  {
    "categories": [
      {
        "id": "string",
        "name": "string",
        "slug": "string",
        "description": "string"
      }
    ]
  }
  ```
- **Consumido por:** Frontend (filtros)
- **Prioridad:** 🔴 ALTA

---

### **4. Catalog Service - Collections** 🟡 MEDIA

#### Endpoint: `GET /api/catalog/collections`
- **Propósito:** Listar todas las colecciones
- **Autenticación:** Público
- **Response esperado:**
  ```json
  {
    "collections": [
      {
        "id": "string",
        "name": "string",
        "slug": "string",
        "description": "string",
        "releaseDate": "date"
      }
    ]
  }
  ```
- **Consumido por:** Frontend (filtros)
- **Prioridad:** 🟡 MEDIA (no crítico para MVP)

---

## 🔗 Relaciones con Otros Servicios

### **Con Java Backend (goat_listing):**

```
Java Backend (8081) ──HTTP──> Python Backend (8082)
```

#### Flujos de Integración:

1. **OTP Flow (✅ Implementado):**
   ```
   Java: POST /api/auth/register
     └─> Python: POST /api/auth/otp
         └─> Envía email con OTP
   
   Java: POST /api/auth/verify
     └─> Python: POST /api/auth/verify
         └─> Valida OTP y marca email como confirmado
   ```

2. **Catalog Validation Flow (❌ Pendiente):**
   ```
   Java: POST /api/listings
     └─> Java: CreateListingUseCase
         └─> Python: GET /api/catalog/sneakers/{sku}
             ├─> 200 OK: SKU existe → Crear listing
             └─> 404 Not Found: SKU no existe → Error 400
   ```

### **Con Frontend React:**

```
Frontend (5173) ──HTTP──> Python Backend (8082)
```

#### Endpoints que Frontend puede consumir directamente:

- `GET /api/catalog/sneakers` - Búsqueda de sneakers (autocompletado)
- `GET /api/catalog/brands` - Lista de marcas (filtros)
- `GET /api/catalog/categories` - Lista de categorías (filtros)
- `GET /api/catalog/collections` - Lista de colecciones (filtros)

**Nota:** El frontend puede llamar directamente a Python para catálogo, pero para listings siempre debe usar Java.

---

## 📊 Priorización de Tareas

### 🔴 **FASE 1 - CRÍTICO (MVP Core)**

#### 1. Catalog Domain - Entidades y Value Objects (3-4 horas)
- **Dependencias:** Ninguna
- **Bloquea:** Use Cases y Persistence
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `catalog/domain/entities/sneaker.py` - Entidad Sneaker
  - [ ] `catalog/domain/entities/brand.py` - Entidad Brand
  - [ ] `catalog/domain/entities/category.py` - Entidad Category
  - [ ] `catalog/domain/entities/collection.py` - Entidad Collection
  - [ ] `catalog/domain/value_objects/sku.py` - Value Object SKU
  - [ ] `catalog/domain/value_objects/brand_slug.py` - Value Object Brand Slug
  - [ ] `catalog/domain/value_objects/media.py` - Value Object Media

#### 2. Catalog Domain - Repositorios (Puertos) (1-2 horas)
- **Dependencias:** Entidades definidas
- **Bloquea:** Use Cases
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `catalog/domain/repositories/sneaker_repository.py` - Puerto
  - [ ] `catalog/domain/repositories/brand_repository.py` - Puerto
  - [ ] `catalog/domain/repositories/category_repository.py` - Puerto
  - [ ] `catalog/domain/repositories/collection_repository.py` - Puerto

#### 3. Catalog Domain - Excepciones (1 hora)
- **Dependencias:** Ninguna
- **Bloquea:** Nada
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `catalog/domain/exceptions/sneaker_not_found_exception.py`
  - [ ] `catalog/domain/exceptions/brand_not_found_exception.py`
  - [ ] `catalog/domain/exceptions/duplicate_sku_exception.py`

#### 4. Catalog Use Cases (4-6 horas)
- **Dependencias:** Domain completo
- **Bloquea:** Controllers
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `get_sneaker_by_sku_use_case.py` - **CRÍTICO para Java**
  - [ ] `search_sneakers_use_case.py`
  - [ ] `create_sneaker_use_case.py`
  - [ ] `update_sneaker_use_case.py`
  - [ ] `get_all_brands_use_case.py`
  - [ ] `get_all_categories_use_case.py`
  - [ ] `get_all_collections_use_case.py`

#### 5. Catalog DTOs (1-2 horas)
- **Dependencias:** Use Cases definidos
- **Bloquea:** Controllers
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `sneaker_response.py`
  - [ ] `sneaker_request.py`
  - [ ] `brand_response.py`
  - [ ] `category_response.py`
  - [ ] `collection_response.py`
  - [ ] `search_filters.py`

#### 6. Catalog Persistence - MongoDB Models (2-3 horas)
- **Dependencias:** Domain entities
- **Bloquea:** Repositories
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `infrastructure/models/mongo_sneaker.py` - Pydantic model
  - [ ] `infrastructure/models/mongo_brand.py` - Pydantic model
  - [ ] `infrastructure/models/mongo_category.py` - Pydantic model
  - [ ] `infrastructure/models/mongo_collection.py` - Pydantic model

#### 7. Catalog Persistence - MongoDB Repositories (3-4 horas)
- **Dependencias:** Models y Domain repositories
- **Bloquea:** Use Cases no pueden ejecutarse
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `infrastructure/persistence/mongo_sneaker_repository.py`
  - [ ] `infrastructure/persistence/mongo_brand_repository.py`
  - [ ] `infrastructure/persistence/mongo_category_repository.py`
  - [ ] `infrastructure/persistence/mongo_collection_repository.py`

#### 8. Catalog MongoDB Indexes (1-2 horas)
- **Dependencias:** Collections definidas
- **Bloquea:** Performance en producción
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] Actualizar `shared/infrastructure/mongo_indexes.py`
  - [ ] Índice único en `sneakers.sku`
  - [ ] Índices para búsquedas: brand, category, gender, collection
  - [ ] Índice de texto para búsqueda por nombre/modelo
  - [ ] Integrar en inicialización automática

#### 9. Catalog Controllers (2-3 horas)
- **Dependencias:** Use Cases + Persistence completos
- **Bloquea:** Java y Frontend no pueden consumir APIs
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `entrypoints/rest/sneaker_controller.py`
    - `GET /api/catalog/sneakers/{sku}` - **CRÍTICO para Java**
    - `GET /api/catalog/sneakers` - Búsqueda
    - `POST /api/catalog/sneakers` - Crear
    - `PUT /api/catalog/sneakers/{sku}` - Actualizar
  - [ ] `entrypoints/rest/brand_controller.py`
    - `GET /api/catalog/brands`
  - [ ] `entrypoints/rest/category_controller.py`
    - `GET /api/catalog/categories`
  - [ ] `entrypoints/rest/collection_controller.py`
    - `GET /api/catalog/collections`
  - [ ] Registrar routers en `app_factory.py`

#### 10. Catalog Seed Data (1-2 horas)
- **Dependencias:** Persistence completo
- **Bloquea:** Testing y desarrollo
- **Prioridad:** 🔴 ALTA
- **Tareas:**
  - [ ] `scripts/seed_catalog.py`
  - [ ] Insertar marcas comunes (Nike, Adidas, etc.)
  - [ ] Insertar categorías (Running, Basketball, etc.)
  - [ ] Insertar algunos sneakers de ejemplo

**Total Fase 1:** 19-27 horas

---

### 🟡 **FASE 2 - MEJORAS (Post-MVP)**

#### 11. Autenticación para Catalog Admin (2-3 horas)
- Endpoints POST/PUT requieren autenticación
- Validar roles (admin)

#### 12. Cache para Brands/Categories (1-2 horas)
- Cachear listas de brands y categories (cambian poco)

---

## ✅ Criterios de Aceptación

### Catalog Service - Sneakers:
- [ ] `GET /api/catalog/sneakers/{sku}` retorna 200 si SKU existe
- [ ] `GET /api/catalog/sneakers/{sku}` retorna 404 si SKU no existe
- [ ] `GET /api/catalog/sneakers` permite búsqueda con filtros
- [ ] `GET /api/catalog/sneakers` soporta paginación
- [ ] SKU es único en la base de datos
- [ ] Índices MongoDB creados correctamente

### Catalog Service - Brands/Categories/Collections:
- [ ] `GET /api/catalog/brands` retorna lista completa
- [ ] `GET /api/catalog/categories` retorna lista completa
- [ ] `GET /api/catalog/collections` retorna lista completa

### Integración con Java:
- [ ] Java puede validar SKU antes de crear listing
- [ ] Respuesta es rápida (< 200ms)
- [ ] Manejo de errores apropiado (404, timeout)

---

## 🚨 Riesgos y Dependencias Críticas

### **Bloqueantes:**
1. **MongoDB debe estar configurado** con colecciones `catalog.*`
   - **Estado:** ✅ MongoDB funcionando, pendiente crear colecciones

2. **Índices MongoDB** deben crearse para performance
   - **Mitigación:** Integrar en inicialización automática

### **No Bloqueantes:**
- OTP Service ya funciona independientemente
- Catalog puede desarrollarse sin depender de otros servicios

---

## 📝 Notas de Implementación

1. **Arquitectura:** Seguir Clean Architecture y DDD (como está en OTP)
2. **Estructura:** Usar misma estructura que `tokens/otps/`
3. **Validaciones:** Validar en dominio (entities) y en use cases
4. **Errores:** Usar excepciones de dominio apropiadas
5. **Tests:** Crear tests unitarios para use cases críticos
6. **No cambiar modelos:** Estructura de MongoDB debe seguir esquema definido
7. **Performance:** Usar índices MongoDB apropiados para búsquedas

---

## 🔄 Orden de Implementación Recomendado

### **Día 1-2: Domain Layer**
1. Entidades y Value Objects
2. Repositorios (puertos)
3. Excepciones

### **Día 3: Application Layer**
1. Use Cases
2. DTOs

### **Día 4-5: Infrastructure Layer**
1. MongoDB Models
2. MongoDB Repositories
3. Índices MongoDB

### **Día 6: Entrypoints**
1. Controllers REST
2. Registrar routers

### **Día 7: Seed Data y Testing**
1. Script de seed
2. Tests de integración
3. Validar integración con Java

---

**Última actualización:** 2025-01-21


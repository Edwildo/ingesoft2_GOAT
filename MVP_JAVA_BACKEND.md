# MVP - Java Backend (goat_listing)

**Servicio:** Identity, Navigation & Listing Service  
**Puerto:** 8081  
**Base de Datos:** PostgreSQL (schemas: `identity`, `navigation`, `listing`)  
**Tecnología:** Spring Boot, JPA, PostgreSQL

---

## 📋 Estado Actual

### ✅ Implementado:
- Autenticación completa (`/api/auth/*`)
  - Registro de usuarios
  - Login con JWT
  - Integración con Python para OTP
  - Confirmación de email
- Health check (`/api/health`)
- Configuración de seguridad (JWT, CORS)
- Integración con Python OTP Service

### ❌ Pendiente para MVP:
- Navigation/Menus Service
- Listings Service
- Integración con Python Catalog Service

---

## 🎯 Objetivos del MVP

1. **Navigation Service:** Proporcionar menús dinámicos al frontend según roles
2. **Listings Service:** Permitir a sellers crear, gestionar y publicar listings
3. **Catalog Integration:** Validar SKUs de sneakers con Python antes de crear listings

---

## 📥 Entradas Esperadas (Dependencias)

### **De Python Backend (goat_catalog):**

#### 1. OTP Service (✅ Ya implementado)
- **Endpoint:** `POST http://localhost:8082/api/auth/otp`
- **Propósito:** Generar códigos OTP para confirmación de email
- **Input:** `{ "email": string, "purpose": string }`
- **Output:** `{ "success": boolean, "message": string, "expiresInMinutes": number }`
- **Estado:** ✅ Funcionando

#### 2. OTP Verification (✅ Ya implementado)
- **Endpoint:** `POST http://localhost:8082/api/auth/verify`
- **Propósito:** Validar códigos OTP
- **Input:** `{ "email": string, "otp": string, "purpose": string }`
- **Output:** `{ "success": boolean, "valid": boolean, "message": string }`
- **Estado:** ✅ Funcionando

#### 3. Catalog Service (❌ Pendiente - CRÍTICO)
- **Endpoint:** `GET http://localhost:8082/api/catalog/sneakers/{sku}`
- **Propósito:** Validar que un SKU de sneaker existe antes de crear listing
- **Input:** SKU (path parameter)
- **Output esperado:** 
  ```json
  {
    "sku": "string",
    "brand": "string",
    "model": "string",
    "gender": "string",
    "exists": true
  }
  ```
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Listings**
- **Prioridad:** 🔴 CRÍTICA

### **De Base de Datos:**

#### PostgreSQL - Schema `identity` (✅ Listo)
- Tabla `users` - Usuarios del sistema
- Tabla `roles` - Roles (SELLER, BUYER, etc.)
- Tabla `users_roles` - Asignación de roles
- **Estado:** ✅ Schema creado y funcionando

#### PostgreSQL - Schema `navigation` (✅ Listo)
- Tabla `menus` - Menús jerárquicos
- Tabla `roles_menus` - Asignación de menús a roles
- **Estado:** ✅ Schema creado, pendiente implementación de servicio

#### PostgreSQL - Schema `listing` (✅ Listo)
- Tabla `listings` - Publicaciones de sellers
- Enum `listing_status` (DRAFT, PUBLISHED, ARCHIVED)
- **Estado:** ✅ Schema creado, pendiente implementación de servicio

---

## 📤 Salidas Esperadas (APIs y Resultados)

### **1. Navigation Service** 🔴 CRÍTICO

#### Endpoint: `GET /api/navigation/menus`
- **Propósito:** Obtener menús dinámicos según rol del usuario
- **Autenticación:** Opcional (si hay token, incluir menús del rol)
- **Response esperado:**
  ```json
  {
    "menus": [
      {
        "id": "uuid",
        "name": "string",
        "route": "string",
        "icon": "string",
        "menuOrder": 0,
        "isPublic": true,
        "children": []
      }
    ]
  }
  ```
- **Consumido por:** Frontend React
- **Prioridad:** 🔴 ALTA (necesario para navegación)

---

### **2. Listings Service** 🔴 CRÍTICO

#### Endpoint: `GET /api/listings`
- **Propósito:** Obtener listings públicos para shop
- **Autenticación:** Público
- **Query params:** `?brand=string&size=string&condition=string&gender=string&color=string&minPrice=number&maxPrice=number&page=number&size=number`
- **Response esperado:**
  ```json
  {
    "listings": [
      {
        "id": "uuid",
        "sellerId": "uuid",
        "sneakerSku": "string",
        "size": "string",
        "condition": "string",
        "gender": "string",
        "brand": "string",
        "color": "string",
        "price": 0.00,
        "status": "PUBLISHED",
        "coverImage": "string",
        "createdAt": "timestamp"
      }
    ],
    "total": 0,
    "page": 0,
    "size": 20
  }
  ```
- **Consumido por:** Frontend React (Shop Page)
- **Prioridad:** 🔴 ALTA

#### Endpoint: `GET /api/listings/{id}`
- **Propósito:** Obtener detalle de un listing
- **Autenticación:** Público
- **Response esperado:** Mismo formato que item de lista
- **Consumido por:** Frontend React (Listing Detail Page)
- **Prioridad:** 🔴 ALTA

#### Endpoint: `GET /api/listings/mine`
- **Propósito:** Obtener listings del seller autenticado
- **Autenticación:** Requerida (JWT) + Rol SELLER
- **Query params:** `?status=DRAFT|PUBLISHED|ARCHIVED&page=number&size=number`
- **Response esperado:** Mismo formato que `GET /api/listings`
- **Consumido por:** Frontend React (Seller Panel)
- **Prioridad:** 🔴 ALTA

#### Endpoint: `POST /api/listings`
- **Propósito:** Crear nuevo listing (estado DRAFT)
- **Autenticación:** Requerida (JWT) + Rol SELLER
- **Request body:**
  ```json
  {
    "sneakerSku": "string",
    "size": "string",
    "condition": "string",
    "gender": "string",
    "brand": "string",
    "color": "string",
    "price": 0.00,
    "coverImage": "string"
  }
  ```
- **Validaciones:**
  - SKU debe existir en Python Catalog (llamar a `GET /api/catalog/sneakers/{sku}`)
  - Precio debe ser positivo
  - Todos los campos requeridos
- **Response esperado:**
  ```json
  {
    "id": "uuid",
    "status": "DRAFT",
    "message": "Listing creado exitosamente"
  }
  ```
- **Consumido por:** Frontend React (Create Listing Form)
- **Prioridad:** 🔴 ALTA

#### Endpoint: `PUT /api/listings/{id}`
- **Propósito:** Actualizar listing (solo si status = DRAFT)
- **Autenticación:** Requerida (JWT) + Rol SELLER + Owner del listing
- **Request body:** Mismo formato que POST
- **Validaciones:**
  - Solo listings en estado DRAFT pueden editarse
  - El seller debe ser el dueño del listing
- **Response esperado:** Listing actualizado
- **Consumido por:** Frontend React (Edit Listing Form) 
- **Prioridad:** 🔴 ALTA

#### Endpoint: `PUT /api/listings/{id}/publish`
- **Propósito:** Publicar listing (DRAFT → PUBLISHED)
- **Autenticación:** Requerida (JWT) + Rol SELLER + Owner del listing
- **Validaciones:**
  - Listing debe estar en estado DRAFT
  - Todos los campos requeridos deben estar completos
- **Response esperado:** Listing con status = PUBLISHED
- **Consumido por:** Frontend React (Seller Panel)
- **Prioridad:** 🔴 ALTA

#### Endpoint: `PUT /api/listings/{id}/archive`
- **Propósito:** Archivar listing (PUBLISHED → ARCHIVED)
- **Autenticación:** Requerida (JWT) + Rol SELLER + Owner del listing
- **Response esperado:** Listing con status = ARCHIVED
- **Consumido por:** Frontend React (Seller Panel)
- **Prioridad:** 🔴 ALTA

---

## 🔗 Relaciones con Otros Servicios

### **Con Python Backend (goat_catalog):**

```
Java Backend (8081) ──HTTP──> Python Backend (8082)
```

#### Flujos de Integración:

1. **OTP Flow (✅ Implementado):**
   ```
   Java: POST /api/auth/register
     └─> Java: GenerateOtpUseCase
         └─> Python: POST /api/auth/otp
   
   Java: POST /api/auth/verify
     └─> Java: VerifyOtpUseCase
         └─> Python: POST /api/auth/verify
   ```

2. **Catalog Validation Flow (❌ Pendiente):**
   ```
   Java: POST /api/listings
     └─> Java: CreateListingUseCase
         └─> Python: GET /api/catalog/sneakers/{sku}
             └─> Si existe: Crear listing
             └─> Si no existe: Error 400
   ```

### **Con Frontend React:**

```
Frontend (5173) ──HTTP──> Java Backend (8081)
```

#### Endpoints Consumidos por Frontend:

- `GET /api/navigation/menus` - Navegación dinámica
- `GET /api/listings` - Shop público
- `GET /api/listings/{id}` - Detalle de listing
- `GET /api/listings/mine` - Listings del seller
- `POST /api/listings` - Crear listing
- `PUT /api/listings/{id}` - Editar listing
- `PUT /api/listings/{id}/publish` - Publicar listing
- `PUT /api/listings/{id}/archive` - Archivar listing

**Nota:** El frontend NUNCA llama directamente a Python, solo a Java.

---

## 📊 Priorización de Tareas

### 🔴 **FASE 1 - CRÍTICO (MVP Core)**

#### 1. Navigation Service (4-6 horas)
- **Dependencias:** Ninguna (solo PostgreSQL)
- **Bloquea:** Nada (puede desarrollarse en paralelo)
- **Prioridad:** 🔴 ALTA
- **Tareas:**
  - [ ] Domain: Menu entity, MenuRepository port
  - [ ] Use Case: GetMenusUseCase
  - [ ] Persistence: MenuEntity, MenuJpaRepository, PostgreSQLMenuRepository
  - [ ] Controller: NavigationController con `GET /api/navigation/menus`
  - [ ] Security: Endpoint público con autenticación opcional

#### 2. Catalog Integration Adapter (2-3 horas)
- **Dependencias:** Python Catalog Service debe tener `GET /api/catalog/sneakers/{sku}`
- **Bloquea:** CreateListingUseCase
- **Prioridad:** 🔴 ALTA
- **Tareas:**
  - [ ] Crear `CatalogServiceAdapter.java`
  - [ ] Método `validateSneakerSku(String sku)`
  - [ ] Manejo de errores (404, timeout, etc.)

#### 3. Listings Service - Domain & Use Cases (4-5 horas)
- **Dependencias:** Catalog Integration Adapter
- **Bloquea:** Listings Controllers
- **Prioridad:** 🔴 ALTA
- **Tareas:**
  - [ ] Domain: Listing entity, ListingStatus enum, repositories
  - [ ] Use Cases: Create, Update, Publish, Archive, Get, GetMyListings
  - [ ] DTOs: Request y Response objects

#### 4. Listings Service - Persistence (2-3 horas)
- **Dependencias:** Domain definido
- **Bloquea:** Use Cases no pueden ejecutarse
- **Prioridad:** 🔴 ALTA
- **Tareas:**
  - [ ] ListingEntity (JPA)
  - [ ] ListingJpaRepository
  - [ ] PostgreSQLListingRepository

#### 5. Listings Service - Controllers (2-3 horas)
- **Dependencias:** Use Cases + Persistence completos
- **Bloquea:** Frontend no puede consumir APIs
- **Prioridad:** 🔴 ALTA
- **Tareas:**
  - [ ] ListingController con todos los endpoints
  - [ ] Security config para endpoints públicos/privados
  - [ ] Validación de ownership en endpoints de seller

**Total Fase 1:** 14-20 horas

---

### 🟡 **FASE 2 - MEJORAS (Post-MVP)**

#### 6. Filtros Avanzados en Listings (2-3 horas)
- Búsqueda por texto, ordenamiento, paginación mejorada

#### 7. Validaciones Adicionales (1-2 horas)
- Validar que seller existe y está activo
- Validar que precio está en rango razonable

---

## ✅ Criterios de Aceptación

### Navigation Service:
- [ ] Endpoint `/api/navigation/menus` retorna menús públicos
- [ ] Si hay JWT válido, incluye menús del rol del usuario
- [ ] Menús están ordenados por `menu_order`
- [ ] Menús hijos están anidados correctamente

### Listings Service:
- [ ] Sellers pueden crear listings en estado DRAFT
- [ ] Sistema valida SKU con Python antes de crear listing
- [ ] Solo listings DRAFT pueden editarse
- [ ] Listings pueden publicarse (DRAFT → PUBLISHED)
- [ ] Listings pueden archivarse (PUBLISHED → ARCHIVED)
- [ ] Shop público muestra solo listings PUBLISHED
- [ ] Filtros funcionan correctamente (brand, size, condition, etc.)
- [ ] Paginación funciona correctamente

### Integración con Python:
- [ ] Java valida SKU con Python antes de crear listing
- [ ] Manejo de errores cuando Python no está disponible
- [ ] Timeout configurado apropiadamente

---

## 🚨 Riesgos y Dependencias Críticas

### **Bloqueantes:**
1. **Python Catalog Service** debe implementar `GET /api/catalog/sneakers/{sku}` antes de poder crear listings
   - **Mitigación:** Implementar Catalog Service primero o usar mock temporal

2. **PostgreSQL schemas** deben estar creados y migrados
   - **Estado:** ✅ Ya están creados

### **No Bloqueantes:**
- Navigation Service puede desarrollarse independientemente
- Listings Domain puede diseñarse sin Catalog Service (solo validación al final)

---

## 📝 Notas de Implementación

1. **Arquitectura:** Seguir Clean Architecture y DDD (como está en Identity)
2. **Seguridad:** Usar JWT para autenticación, validar roles para autorización
3. **Validaciones:** Validar en dominio (entities) y en use cases
4. **Errores:** Usar excepciones de dominio apropiadas
5. **Tests:** Crear tests unitarios para use cases críticos
6. **No cambiar modelos:** Los schemas de PostgreSQL están definidos y no deben modificarse

---

**Última actualización:** 2025-01-21


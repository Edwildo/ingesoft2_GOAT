# MVP - Frontend React (goat_frontend)

**Aplicación:** React + TypeScript + Vite  
**Puerto:** 5173  
**API Base:** http://localhost:8081 (Java Backend)  
**Catalog API:** http://localhost:8082 (Python Backend - opcional directo)

---

## 📋 Estado Actual

### ✅ Implementado:
- **Autenticación completa:**
  - Login (`/login`)
  - Registro (`/register`)
  - Verificación OTP (`/verify-otp`)
  - Reset password (`/reset-password`)
- **Dashboard básico:**
  - Información del usuario
  - Estado de email confirmado
  - Roles del usuario
- **Infraestructura:**
  - AuthContext para gestión de estado
  - API service configurado
  - Interceptors para JWT
  - Componentes comunes (Button, Card, Input, etc.)

### ❌ Pendiente para MVP:
- **Navigation dinámica** - Menús según roles
- **Shop Page** - Listings públicos
- **Seller Panel** - Gestión de listings
- **Integración con Catalog** - Mostrar información de sneakers

---

## 🎯 Objetivos del MVP

1. **Shop Público:** Permitir a usuarios (autenticados o no) ver listings de sneakers
2. **Seller Panel:** Permitir a sellers crear, editar y publicar listings
3. **Navigation Dinámica:** Mostrar menús según rol del usuario
4. **Catalog Integration:** Mostrar información completa de sneakers desde catálogo

---

## 📥 Entradas Esperadas (Dependencias)

### **De Java Backend (goat_listing):**

#### 1. Navigation Service (❌ Pendiente)
- **Endpoint:** `GET http://localhost:8081/api/navigation/menus`
- **Propósito:** Obtener menús dinámicos según rol
- **Input:** JWT token (opcional en header)
- **Output esperado:**
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
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Navigation**
- **Prioridad:** 🟡 MEDIA (mejora UX, no crítico)

#### 2. Listings Service - Shop (❌ Pendiente)
- **Endpoint:** `GET http://localhost:8081/api/listings`
- **Propósito:** Obtener listings públicos para shop
- **Query params:** `?brand=string&size=string&condition=string&gender=string&color=string&minPrice=number&maxPrice=number&page=number&size=number`
- **Output esperado:**
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
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Shop Page**
- **Prioridad:** 🔴 CRÍTICA

#### 3. Listings Service - Detail (❌ Pendiente)
- **Endpoint:** `GET http://localhost:8081/api/listings/{id}`
- **Propósito:** Obtener detalle de un listing
- **Output esperado:** Mismo formato que item de lista
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Listing Detail**
- **Prioridad:** 🔴 CRÍTICA

#### 4. Listings Service - Seller (❌ Pendiente)
- **Endpoint:** `GET http://localhost:8081/api/listings/mine`
- **Propósito:** Obtener listings del seller autenticado
- **Autenticación:** JWT requerido + rol SELLER
- **Query params:** `?status=DRAFT|PUBLISHED|ARCHIVED&page=number&size=number`
- **Output esperado:** Mismo formato que `GET /api/listings`
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Seller Panel**
- **Prioridad:** 🔴 CRÍTICA

#### 5. Listings Service - Create (❌ Pendiente)
- **Endpoint:** `POST http://localhost:8081/api/listings`
- **Propósito:** Crear nuevo listing
- **Autenticación:** JWT requerido + rol SELLER
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
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Create Listing**
- **Prioridad:** 🔴 CRÍTICA

#### 6. Listings Service - Update (❌ Pendiente)
- **Endpoint:** `PUT http://localhost:8081/api/listings/{id}`
- **Propósito:** Actualizar listing (solo DRAFT)
- **Autenticación:** JWT requerido + rol SELLER + owner
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Edit Listing**
- **Prioridad:** 🔴 CRÍTICA

#### 7. Listings Service - Publish (❌ Pendiente)
- **Endpoint:** `PUT http://localhost:8081/api/listings/{id}/publish`
- **Propósito:** Publicar listing
- **Autenticación:** JWT requerido + rol SELLER + owner
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Seller Panel**
- **Prioridad:** 🔴 CRÍTICA

#### 8. Listings Service - Archive (❌ Pendiente)
- **Endpoint:** `PUT http://localhost:8081/api/listings/{id}/archive`
- **Propósito:** Archivar listing
- **Autenticación:** JWT requerido + rol SELLER + owner
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Seller Panel**
- **Prioridad:** 🔴 CRÍTICA

---

### **De Python Backend (goat_catalog):**

#### 1. Catalog Service - Sneakers Search (❌ Pendiente)
- **Endpoint:** `GET http://localhost:8082/api/catalog/sneakers`
- **Propósito:** Buscar sneakers para autocompletado
- **Query params:** `?search=string&brand=string&page=number&size=number`
- **Output esperado:**
  ```json
  {
    "sneakers": [
      {
        "sku": "string",
        "brand": "string",
        "model": "string",
        "gender": "string",
        "media": {
          "coverImage": "string"
        }
      }
    ],
    "total": 0,
    "page": 0,
    "size": 20
  }
  ```
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para SKU Autocomplete**
- **Prioridad:** 🟡 MEDIA (mejora UX)

#### 2. Catalog Service - Sneaker Detail (❌ Pendiente)
- **Endpoint:** `GET http://localhost:8082/api/catalog/sneakers/{sku}`
- **Propósito:** Obtener información completa del sneaker
- **Output esperado:**
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
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Listing Detail**
- **Prioridad:** 🟡 MEDIA (mejora UX)

#### 3. Catalog Service - Brands (❌ Pendiente)
- **Endpoint:** `GET http://localhost:8082/api/catalog/brands`
- **Propósito:** Obtener lista de marcas para filtros
- **Output esperado:**
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
- **Estado:** ❌ **NO IMPLEMENTADO - BLOQUEANTE para Filtros**
- **Prioridad:** 🟡 MEDIA (mejora UX)

#### 4. Catalog Service - Categories (❌ Pendiente)
- **Endpoint:** `GET http://localhost:8082/api/catalog/categories`
- **Propósito:** Obtener lista de categorías para filtros
- **Estado:** ❌ **NO IMPLEMENTADO**
- **Prioridad:** 🟡 MEDIA

---

## 📤 Salidas Esperadas (Páginas y Componentes)

### **1. Navigation Component** 🟡 MEDIA

#### Componente: `Navbar.tsx`
- **Propósito:** Mostrar menús dinámicos según rol
- **Funcionalidad:**
  - Mostrar menús públicos siempre
  - Mostrar menús del rol si usuario autenticado
  - Navegación entre páginas
- **Dependencias:** Java Navigation Service
- **Prioridad:** 🟡 MEDIA

---

### **2. Shop Page** 🔴 CRÍTICO

#### Página: `ShopPage.tsx`
- **Ruta:** `/shop`
- **Propósito:** Mostrar listings públicos de sneakers
- **Funcionalidad:**
  - Grid de listings con imágenes
  - Mostrar: imagen, brand, model, size, condition, price
  - Paginación
  - Filtros: brand, size, condition, gender, color, price range
- **Dependencias:** 
  - Java Listings Service (`GET /api/listings`)
  - Python Catalog Service (opcional, para brands)
- **Prioridad:** 🔴 CRÍTICA

#### Componente: `ListingCard.tsx`
- **Propósito:** Card individual de listing
- **Muestra:** Imagen, brand, model, size, condition, price
- **Acción:** Link a detalle

#### Componente: `ListingFilters.tsx`
- **Propósito:** Filtros para búsqueda de listings
- **Filtros:** Brand, Size, Condition, Gender, Color, Price Range
- **Dependencias:** Python Catalog Service (para obtener brands)

#### Página: `ListingDetailPage.tsx`
- **Ruta:** `/shop/:id`
- **Propósito:** Mostrar detalle completo de un listing
- **Funcionalidad:**
  - Información del listing (desde Java)
  - Información del sneaker (desde Python Catalog)
  - Imágenes del sneaker
  - Botón "Comprar" (deshabilitado en MVP, solo UI)
- **Dependencias:**
  - Java Listings Service (`GET /api/listings/{id}`)
  - Python Catalog Service (`GET /api/catalog/sneakers/{sku}`)

---

### **3. Seller Panel** 🔴 CRÍTICO

#### Página: `MyListingsPage.tsx`
- **Ruta:** `/seller/listings`
- **Propósito:** Mostrar listings del seller
- **Funcionalidad:**
  - Tabla/Grid de mis listings
  - Mostrar estado (DRAFT, PUBLISHED, ARCHIVED)
  - Filtros por estado
  - Acciones: Editar, Publicar, Archivar
- **Dependencias:** Java Listings Service (`GET /api/listings/mine`)
- **Prioridad:** 🔴 CRÍTICA

#### Página: `CreateListingPage.tsx`
- **Ruta:** `/seller/listings/new`
- **Propósito:** Crear nuevo listing
- **Funcionalidad:**
  - Formulario con campos: SKU, size, condition, gender, brand, color, price, coverImage
  - Autocompletado de SKU (llamar a Python Catalog)
  - Validación de campos
  - Submit a Java (`POST /api/listings`)
- **Dependencias:**
  - Java Listings Service (`POST /api/listings`)
  - Python Catalog Service (`GET /api/catalog/sneakers?search=`)
- **Prioridad:** 🔴 CRÍTICA

#### Página: `EditListingPage.tsx`
- **Ruta:** `/seller/listings/:id/edit`
- **Propósito:** Editar listing existente
- **Funcionalidad:**
  - Formulario pre-llenado con datos del listing
  - Solo permitir editar si status = DRAFT
  - Validación de campos
  - Submit a Java (`PUT /api/listings/{id}`)
- **Dependencias:** Java Listings Service (`GET /api/listings/{id}`, `PUT /api/listings/{id}`)
- **Prioridad:** 🔴 CRÍTICA

#### Componente: `SkuAutocomplete.tsx`
- **Propósito:** Autocompletado de SKU al crear listing
- **Funcionalidad:**
  - Llamar a Python Catalog mientras usuario escribe
  - Mostrar sugerencias de sneakers
  - Seleccionar SKU
- **Dependencias:** Python Catalog Service (`GET /api/catalog/sneakers?search=`)

---

## 🔗 Relaciones con Otros Servicios

### **Con Java Backend (goat_listing):**

```
Frontend (5173) ──HTTP──> Java Backend (8081)
```

#### Flujos de Integración:

1. **Navigation Flow:**
   ```
   Frontend: App carga
     └─> Frontend: getMenus() (con JWT si existe)
         └─> Java: GET /api/navigation/menus
             └─> Renderizar Navbar con menús
   ```

2. **Shop Flow:**
   ```
   Frontend: Usuario visita /shop
     └─> Frontend: getListings(filters)
         └─> Java: GET /api/listings?brand=...&size=...
             └─> Renderizar grid de listings
   ```

3. **Listing Detail Flow:**
   ```
   Frontend: Usuario hace click en listing
     └─> Frontend: getListingById(id)
         └─> Java: GET /api/listings/{id}
             └─> Frontend: getSneakerBySku(sku) (opcional, a Python)
                 └─> Renderizar detalle completo
   ```

4. **Seller Panel Flow:**
   ```
   Frontend: Seller visita /seller/listings
     └─> Frontend: getMyListings() (con JWT)
         └─> Java: GET /api/listings/mine
             └─> Renderizar tabla de listings
   ```

5. **Create Listing Flow:**
   ```
   Frontend: Seller crea listing
     └─> Frontend: searchSneakers(query) (a Python, para autocompletado)
     └─> Frontend: createListing(data) (con JWT)
         └─> Java: POST /api/listings
             └─> Java valida SKU con Python
             └─> Crear listing en DRAFT
   ```

### **Con Python Backend (goat_catalog):**

```
Frontend (5173) ──HTTP──> Python Backend (8082)
```

#### Flujos de Integración:

1. **SKU Autocomplete Flow:**
   ```
   Frontend: Usuario escribe en campo SKU
     └─> Frontend: searchSneakers(query)
         └─> Python: GET /api/catalog/sneakers?search=...
             └─> Mostrar sugerencias
   ```

2. **Sneaker Detail Flow:**
   ```
   Frontend: Muestra detalle de listing
     └─> Frontend: getSneakerBySku(sku)
         └─> Python: GET /api/catalog/sneakers/{sku}
             └─> Mostrar información completa del sneaker
   ```

3. **Filters Flow:**
   ```
   Frontend: Carga página de shop
     └─> Frontend: getBrands()
         └─> Python: GET /api/catalog/brands
             └─> Poblar dropdown de filtros
   ```

**Nota:** El frontend puede llamar directamente a Python para catálogo, pero para listings siempre debe usar Java.

---

## 📊 Priorización de Tareas

### 🔴 **FASE 1 - CRÍTICO (MVP Core)**

#### 1. Shop Service - API Client (1-2 horas)
- **Dependencias:** Java Listings Service debe estar listo
- **Bloquea:** Shop Page
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `src/api/listing.service.ts`
    - `getListings(filters?)` - `GET /api/listings`
    - `getListingById(id)` - `GET /api/listings/{id}`
  - [ ] Tipos TypeScript para requests y responses

#### 2. Shop Page - Listado (3-4 horas)
- **Dependencias:** Listing Service API
- **Bloquea:** Usuarios no pueden ver listings
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `src/pages/shop/ShopPage.tsx`
  - [ ] `src/pages/shop/ShopPage.module.css`
  - [ ] Grid de listings
  - [ ] Paginación básica

#### 3. Listing Card Component (1-2 horas)
- **Dependencias:** Shop Page
- **Bloquea:** Nada
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `src/components/shop/ListingCard.tsx`
  - [ ] `src/components/shop/ListingCard.module.css`
  - [ ] Link a detalle

#### 4. Listing Detail Page (2-3 horas)
- **Dependencias:** Listing Service API
- **Bloquea:** Usuarios no pueden ver detalle
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `src/pages/shop/ListingDetailPage.tsx`
  - [ ] `src/pages/shop/ListingDetailPage.module.css`
  - [ ] Mostrar información del listing
  - [ ] Integración opcional con Python para info del sneaker

#### 5. Seller Service - API Client (1-2 horas)
- **Dependencias:** Java Listings Service debe estar listo
- **Bloquea:** Seller Panel
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `src/api/seller.service.ts`
    - `getMyListings()` - `GET /api/listings/mine`
    - `createListing(data)` - `POST /api/listings`
    - `updateListing(id, data)` - `PUT /api/listings/{id}`
    - `publishListing(id)` - `PUT /api/listings/{id}/publish`
    - `archiveListing(id)` - `PUT /api/listings/{id}/archive`

#### 6. My Listings Page (3-4 horas)
- **Dependencias:** Seller Service API
- **Bloquea:** Sellers no pueden ver sus listings
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `src/pages/seller/MyListingsPage.tsx`
  - [ ] `src/pages/seller/MyListingsPage.module.css`
  - [ ] Tabla/Grid de listings
  - [ ] Filtros por estado
  - [ ] Acciones: Editar, Publicar, Archivar

#### 7. Create Listing Page (4-5 horas)
- **Dependencias:** Seller Service API + Python Catalog (opcional)
- **Bloquea:** Sellers no pueden crear listings
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `src/pages/seller/CreateListingPage.tsx`
  - [ ] `src/pages/seller/CreateListingPage.module.css`
  - [ ] Formulario con validación
  - [ ] Autocompletado de SKU (si Python está listo)
  - [ ] Submit y manejo de errores

#### 8. Edit Listing Page (3-4 horas)
- **Dependencias:** Seller Service API
- **Bloquea:** Sellers no pueden editar listings
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] `src/pages/seller/EditListingPage.tsx`
  - [ ] `src/pages/seller/EditListingPage.module.css`
  - [ ] Cargar datos del listing
  - [ ] Validar que status = DRAFT
  - [ ] Formulario pre-llenado
  - [ ] Submit y manejo de errores

#### 9. Rutas y Protección (1-2 horas)
- **Dependencias:** Todas las páginas
- **Bloquea:** Navegación no funciona
- **Prioridad:** 🔴 CRÍTICA
- **Tareas:**
  - [ ] Agregar rutas en `App.tsx`
  - [ ] Proteger rutas de seller (requiere autenticación + rol SELLER)
  - [ ] Redirect si no tiene permisos

**Total Fase 1:** 19-28 horas

---

### 🟡 **FASE 2 - MEJORAS (Post-MVP)**

#### 10. Navigation Dinámica (3-4 horas)
- Navbar con menús dinámicos
- Depende de Java Navigation Service

#### 11. Listing Filters Component (2-3 horas)
- Filtros avanzados en Shop
- Depende de Python Catalog Service

#### 12. SKU Autocomplete Component (2-3 horas)
- Autocompletado mejorado
- Depende de Python Catalog Service

#### 13. Catalog Integration (2-3 horas)
- Mostrar información completa de sneakers
- Depende de Python Catalog Service

**Total Fase 2:** 9-13 horas

---

## ✅ Criterios de Aceptación

### Shop Page:
- [ ] Usuarios pueden ver listings públicos
- [ ] Grid muestra imágenes, brand, model, size, condition, price
- [ ] Paginación funciona correctamente
- [ ] Click en listing lleva a detalle
- [ ] Filtros básicos funcionan (si implementados)

### Listing Detail Page:
- [ ] Muestra información completa del listing
- [ ] Muestra información del sneaker (si Python está integrado)
- [ ] Imágenes se muestran correctamente

### Seller Panel:
- [ ] Sellers pueden ver sus listings
- [ ] Sellers pueden crear listings
- [ ] Sellers pueden editar listings DRAFT
- [ ] Sellers pueden publicar listings
- [ ] Sellers pueden archivar listings
- [ ] Validación de campos funciona
- [ ] Manejo de errores apropiado

### Navigation:
- [ ] Menús se muestran según rol (si implementado)
- [ ] Navegación entre páginas funciona

---

## 🚨 Riesgos y Dependencias Críticas

### **Bloqueantes:**
1. **Java Listings Service** debe estar completo antes de poder desarrollar Shop y Seller Panel
   - **Mitigación:** Desarrollar UI con datos mock primero

2. **Java Navigation Service** debe estar listo para Navigation dinámica
   - **Mitigación:** Usar menús estáticos temporalmente

### **No Bloqueantes:**
- Python Catalog Service puede desarrollarse en paralelo
- Filtros y autocompletado son mejoras, no críticos para MVP

---

## 📝 Notas de Implementación

1. **API Calls:** Usar `fetch` o `axios` (ya configurado en `api.config.ts`)
2. **Estado:** Usar React Context o hooks para estado global
3. **Routing:** Usar React Router (ya configurado)
4. **Autenticación:** JWT en localStorage, incluir en headers
5. **Validación:** Validar en frontend antes de enviar a backend
6. **Errores:** Mostrar mensajes de error apropiados al usuario
7. **Loading:** Mostrar estados de carga mientras se hacen requests
8. **Responsive:** Diseño debe funcionar en móvil y desktop

---

## 🔄 Orden de Implementación Recomendado

### **Semana 1: Shop Público**
1. Día 1: Listing Service API + Shop Page básico
2. Día 2: Listing Card + Listing Detail Page
3. Día 3: Filtros básicos (si tiempo)

### **Semana 2: Seller Panel**
1. Día 1: Seller Service API + My Listings Page
2. Día 2: Create Listing Page
3. Día 3: Edit Listing Page + Rutas

### **Semana 3: Mejoras**
1. Día 1-2: Navigation dinámica
2. Día 3-4: Integración con Python Catalog
3. Día 5: Testing y refinamiento

---

**Última actualización:** 2025-01-21


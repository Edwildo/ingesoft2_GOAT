Perfecto.
Voy a **reescribir el documento COMPLETO**, desde cero, totalmente alineado con **TU DDL**, con **DDD**, **Arquitectura Hexagonal**, **Screaming Architecture**, y reflejando la realidad del microservicio Java:

### 👉 **Este microservicio NO es solo “listing-service”.**

Por tu DDL, este microservicio maneja **3 bounded contexts completos**:

1. **Identity** — usuarios, roles, asignación de roles
2. **Navigation** — menús dinámicos por rol y públicos
3. **Listing** — publicaciones de sneakers por vendedores

Es un **microservicio modular**, altamente estructurado, que probablemente en una fase futura se dividirá en 3 servicios separados, pero HOY vive junto en el mismo proyecto Java.

A continuación tienes el documento **totalmente reescrito**, **cerrado**, **correcto**, **profesional**, y **listo para incluir en tu repositorio**.

---

# 🚀 **`JAVA_POSTGRES_ARCHITECTURE.md`**

### *GOAT Identity + Navigation + Listing Service*

### Domain-Driven Design • Hexagonal Architecture • Screaming Architecture

### PostgreSQL (identity, navigation, listing)

---

```md
# GOAT Identity + Navigation + Listing Service (Java)
## DDD • Hexagonal Architecture • Screaming Architecture
### PostgreSQL Schemas: identity, navigation, listing

---

# 1. Propósito General del Microservicio

Este servicio Java reúne tres bounded contexts fundamentales para la aplicación GOAT-like:

---

## 🟢 1.1 Identity Context (Usuarios y Roles)
Maneja:
- Usuarios del sistema (Sellers, Buyers, Super Admin, Support, Client)
- Autenticación basada en credenciales (email + hash)
- Confirmación de correo
- Estado activo/inactivo
- Roles del usuario
- Asignación de roles (many-to-many)

---

## 🟢 1.2 Navigation Context (Menús Dinámicos)
Maneja:
- Menús públicos (Home, Shop)
- Menús privados por rol (paneles, administración, herramientas)
- Árbol jerárquico de navegación
- Permisos de visibilidad por rol
- Orden y estructura del menú  
Permite construir el menú del frontend sin redeploy.

---

## 🟢 1.3 Listing Context (Publicaciones de Sneakers)
Maneja:
- Creación, edición y publicación de listings
- Estados del listing (DRAFT, PUBLISHED, ARCHIVED)
- Precio, talla, condición, color, género, marca
- Imagen de portada (URL S3)
- Enlace con catálogo canónico por `sneaker_sku` (Mongo)

Es el corazón de la interacción Seller → Marketplace.

---

# 2. Filosofía Arquitectónica

Este microservicio adopta:

---

## 2.1 Domain-Driven Design (DDD)
El código se estructura por dominios, no por tecnologías:

- **Identity Domain**
- **Navigation Domain**
- **Listing Domain**

Cada contexto contiene:
- Entidades
- Value Objects
- Agregados
- Dominios ricos
- Reglas de negocio encapsuladas

---

## 2.2 Arquitectura Hexagonal (Ports & Adapters)

El microservicio sigue la estructura:

```

domain -> application -> ports -> adapters -> infrastructure -> entrypoints

```

### Beneficios:
- Separación estricta entre lógica de dominio y framework
- Embedded domain rules
- Testabilidad máxima
- Adaptadores intercambiables
- Limpieza conceptual a largo plazo

---

## 2.3 Screaming Architecture

La estructura del proyecto “grita” el dominio:

```

src/main/java/com/goat
└── identity
└── navigation
└── listing

```

No se agrupa por controllers/services/repos.  
Se agrupa por **bounded context**.

---

# 3. Modelo de Datos (PostgreSQL)

Totalmente basado en el DDL oficial.  
Organizado por dominios.

---

# 3.1 IDENTITY CONTEXT

## Tabla: `identity.users`
Propósito:
- Identificar a Sellers y Buyers
- Control de autenticación
- Estado y verificación

Campos clave:
- `email` único
- `password_hash` (bcrypt/argon2)
- `email_confirmed`
- `is_active`  
- `updated_at` vía trigger

---

## Tabla: `identity.roles`
Roles predefinidos:
- SUPER_ADMIN
- SELLER
- BUYER
- SUPPORT
- CLIENT

Cada servicio del ecosistema puede usar estos roles para RBAC.

---

## Tabla: `identity.users_roles`
Relación many-to-many Seller/Buyer/Admin.

---

## Vista: `identity.vw_users_with_roles`
Devuelve:
- Usuario
- Roles agregados como array
- Estado, fechas, confirmación

---

# 3.2 NAVIGATION CONTEXT

## Tabla: `navigation.menus`
Define la estructura del menú del frontend:

Campos:
- `parent_id` → jerarquía (menú/submenú)
- `name` → nombre visible
- `route` → ruta del frontend
- `icon`
- `menu_order`
- `is_public`

### Características:
- Árbol dinámico configurable desde DB
- Menús públicos sin autenticación
- Menús privados según rol

---

## Tabla: `navigation.roles_menus`
Define qué rol puede ver cada menú.

---

## Vista: `navigation.vw_menus_by_role`
Devuelve:
- Menú
- Roles permitidos (array)
- Detalles de visualización

Esto alimenta el frontend para generar la navegación.

---

# 3.3 LISTING CONTEXT

## ENUM `listing.listing_status`
- DRAFT  
- PUBLISHED  
- ARCHIVED  

---

## Tabla: `listing.listings`

Campos clave:
- `seller_id` → FK a `identity.users`
- `sneaker_sku` → FK lógica al catálogo Mongo
- `size`, `condition`, `gender`, `color`
- `brand` (redundado para filtros rápidos)
- `price`
- `cover_image`
- `status`
- `created_at`, `updated_at`

### Reglas de integridad:
- Price > 0
- Solo sellers activos
- No se borra un listing publicado, se archiva

---

## Vista: `listing.vw_published_listings`
Devuelve:
- Listing listo para Shop
- Información del seller (email)

---

# 4. Reglas de Negocio (por contexto)

---

# 4.1 Identity Rules

### Autenticación
- Email único
- Hash seguro
- `email_confirmed=true` antes de usar funciones sensibles
- Solo usuarios activos pueden crear listings

### Roles
- Un usuario puede tener múltiples roles
- Roles determinan visibilidad del menú

---

# 4.2 Navigation Rules

- Menús públicos siempre visibles
- Menús privados se filtran por rol
- Estructura jerárquica
- Orden definido por `menu_order`

**El menú se construye en tiempo real desde la DB.**

---

# 4.3 Listing Rules

### Estados
- DRAFT → editable
- PUBLISHED → NO editable en atributos vitales
- ARCHIVED → estado terminal

### Atributos vitales (no editables al publicar)
- tamaño
- condición
- color
- marca
- género
- precio
- SKU

Si se necesita un cambio:
1. ARCHIVE listing  
2. CREATE new listing  

### SKU
- Debe existir en catálogo (Mongo / Python)
- Este servicio NO gestiona catálogo

---

# 5. Arquitectura Hexagonal por Contexto

Cada bounded context tiene:

## 5.1 Domain Layer
- Entidades ricas
- Value objects
- Policies

Ejemplos:
- `Listing`
- `ListingStatus`
- `Email`
- `User`
- `Role`

---

## 5.2 Application Layer
Casos de uso:

### Identity
- createUser
- assignRole
- confirmEmail

### Navigation
- buildMenuForUser
- createMenu
- assignMenuToRole

### Listing
- createListing
- updateDraftListing
- publishListing
- archiveListing

---

## 5.3 Ports (Interfaces)
- `UserRepository`
- `RoleRepository`
- `MenuRepository`
- `ListingRepository`
- `CatalogLookupPort` (consulta a Python/Mongo)
- `MediaUploadPort` (S3)

---

## 5.4 Adapters (Implementaciones)
- PostgreSQL (Spring Data)
- AWS S3 client
- REST client hacia catalog-service

---

## 5.5 Entrypoints
- REST Controllers (Spring Boot)

---

# 6. Bounded Context Interactions

| Contexto | Interacción | Dirección |
|---------|-------------|-----------|
| Identity → Navigation | Menú según roles | interno |
| Identity → Listing | Seller activo | interno |
| Listing → Catalog (Python) | Validar SKU | REST |
| Listing → Media | Guardar imágenes | REST/S3 |

---

# 7. Escalabilidad

- PostgreSQL con índices optimizados para filtros de shop
- Cada contexto puede escindirse en microservicio independiente en el futuro
- Adaptadores desacoplados (Ports/Adapters)
- Dominio completamente estable independientemente del framework

---

# 8. Posibles futuras divisiones en microservicios

A partir de este diseño modular:

- **identity-service**
- **navigation-service**
- **listing-service**
- **media-service**
- **catalog-service** (ya en Python)
- **order-service**
- **payment-service**

El diseño actual soporta este splitting sin refactor agresivo.

---

# 9. Conclusión

El microservicio Java implementa **tres bounded contexts críticos**:

- Identity  
- Navigation  
- Listing  

Usa **DDD**, **Hexagonal**, y **Screaming Architecture**, y su modelo de datos está **totalmente alineado** a PostgreSQL según tu DDL.  
Es escalable, modular, y preparado para integrarse en una arquitectura de microservicios madura.

```

---

# ¿Quieres que ahora rehaga **el MD completo para Python + MongoDB** con la misma calidad?

Puedo dejar ambos documentos listos para tu repositorio técnico.

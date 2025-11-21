GOAT Catalog & Authentication Service
Domain-Driven Design • Clean Architecture • Screaming Architecture
MongoDB (TTL + Catálogo canónico)
# GOAT Catalog & Auth Service (Python)
## DDD • Clean Architecture • Screaming Architecture
### MongoDB – Catálogo Canónico + Tokens Efímeros

---

# 1. Propósito General del Microservicio

Este microservicio Python es responsable de dos dominios críticos del ecosistema GOAT-like:

---

## 🟢 1.1 Catalog Context (Catálogo Canónico)
Gestiona el inventario maestro de sneakers y su representación rica para el marketplace:

- Sneakers canónicos (SKU único)
- Marcas
- Categorías
- Colecciones
- Relación con media (S3)
- Atributos dinámicos
- Control de disponibilidad conceptual

Este catálogo es la **fuente de verdad** para:
- El frontend
- El microservicio Java (listings)
- Futuros microservicios (orders, verification)

---

## 🟢 1.2 Auth Token Context (Tokens efímeros)
Gestiona:
- OTPs para registro y login
- Tokens de confirmación de correo
- Tokens de recuperación de contraseña

Todos los tokens:
- Son almacenados con TTL en MongoDB
- Se eliminan automáticamente
- Se guardan como hash (nunca planos)

---

# 2. Filosofía Arquitectónica

Este servicio Python sigue:

## ✅ Clean Architecture (adaptada a Python)
Separación clara:



Domain → Application → Ports → Adapters → Infrastructure → Entrypoints


- Domain: reglas puras
- Application: casos de uso
- Ports: contratos
- Adapters: Mongo, S3, Email gateway
- Entrypoints: FastAPI

---

## ✅ Domain-Driven Design (DDD)
Separa el dominio en dos contextos claros:

- Catalog Domain
- Token Domain

Cada dominio define:
- Entidades
- Value Objects
- Invariantes
- Reglas de negocio

---

## ✅ Screaming Architecture

La estructura del proyecto refleja claramente el dominio:



src/
└── goat_catalog
├── catalog
│ ├── domain
│ ├── application
│ ├── infrastructure
│ └── entrypoints
├── tokens
│ ├── domain
│ ├── application
│ ├── infrastructure
│ └── entrypoints


---

# 3. Modelo de Datos (MongoDB)

---

# 3.1 COLECCIÓN: sneakers

Representa el sneaker canónico.

```json
{
  "_id": ObjectId,
  "sku": "NK-AM90-001",
  "brand": "Nike",
  "model": "Air Max 90",
  "gender": "MEN",
  "sizes": ["US 8", "US 9", "US 10"],
  "price_reference": 499.99,
  "media": {
    "cover": "s3://bucket/img1.jpg",
    "images": [
      "s3://bucket/img2.jpg",
      "s3://bucket/img3.jpg"
    ]
  },
  "categories": ["Sneakers"],
  "collections": ["Just Dropped"],
  "attributes": {
    "material": "Leather",
    "style": "Urban"
  },
  "active": true,
  "createdAt": ISODate(),
  "updatedAt": ISODate()
}

Índices recomendados
db.sneakers.createIndex({ sku: 1 }, { unique: true })
db.sneakers.createIndex({ brand: 1, model: 1 })
db.sneakers.createIndex({ gender: 1 })

3.2 COLECCIÓN: categories
{
  "_id": ObjectId,
  "slug": "sneakers",
  "name": "Sneakers",
  "parent": null,
  "level": 0,
  "active": true
}

3.3 COLECCIÓN: collections
{
  "_id": ObjectId,
  "slug": "just-dropped",
  "name": "Just Dropped",
  "active": true
}

3.4 COLECCIÓN: brands
{
  "_id": ObjectId,
  "slug": "nike",
  "name": "Nike",
  "active": true
}

3.5 COLECCIÓN: otps (TTL)
{
  "email": "user@mail.com",
  "purpose": "REGISTER",
  "otpHash": "abcdef123456",
  "attempts": 0,
  "createdAt": ISODate(),
  "expireAt": ISODate()
}


TTL Index:

db.otps.createIndex({ expireAt: 1 }, { expireAfterSeconds: 0 })

4. Reglas de Negocio
4.1 Catalog Domain

El SKU define unicidad absoluta

Las tallas provienen de un catálogo fijo

Las imágenes se almacenan en S3

El sneaker debe pertenecer a al menos una categoría y marca

active=false elimina visibilidad sin borrado físico

4.2 Token Domain

Los OTPs tienen duración máxima configurada (ej. 5 min)

Máximo de intentos configurables

Hash obligatorio del token

Eliminación automática por TTL

Validación ofuscada para evitar enumeración

5. Clean Architecture Detallada
5.1 Domain Layer
Entidades principales

Sneaker

Category

Brand

Collection

OTPToken

Ejemplo conceptual:

@dataclass
class Sneaker:
    sku: SKU
    brand: Brand
    model: str
    sizes: List[Size]

5.2 Application Layer

Casos de uso:

Catalog

createSneaker

updateSneaker

searchSneakers

listSneakersByFilters

Tokens

generateOTP

validateOTP

generateResetToken

consumeResetToken

5.3 Ports

Interfaces abstractas:

SneakerRepository

TokenRepository

MediaStoragePort

EmailPort

5.4 Infrastructure

Implementaciones concretas:

MongoSneakerRepository

MongoOtpRepository

S3MediaStorage

SMTP/Mail gateway

5.5 Entrypoints

FastAPI controllers:

GET /sneakers
GET /sneakers/{sku}
POST /sneakers
POST /auth/otp
POST /auth/verify

6. Comunicación con Java Service
Origen	Destino	Propósito
Java Listing	Python Catalog	Validar SKU
Java Identity	Python Tokens	Emitir OTP
Frontend	Python	Mostrar catálogo

Todos los endpoints deben ser idempotentes y protegidos.

7. Flujo del sistema
7.1 Flujo OTP

Usuario solicita OTP

Se genera hash + TTL

Se envía por email

Usuario ingresa OTP

Se valida

Se elimina el registro

7.2 Flujo Catálogo

Admin gestiona sneakers

Sneakers visibles para shop

Listings referencian SKU

Front combina catálogo (Mongo) + listings (PG)

8. Escalabilidad

MongoDB permite atributos dinámicos

TTL mantiene BD liviana

Casos de uso desacoplados

Arquitectura lista para proyecciones futuras

Preparado para CQRS

9. Seguridad

Hash para tokens

Protección contra brute-force

Rate limit por IP/email (extensible)

Logs de auditoría opcionales

10. Posible evolución futura

Division en:

catalog-service

auth-token-service

media-service

Cache distribuido (Redis)

Event sourcing para cambios de catálogo

11. Conclusión

Este microservicio Python provee la base flexible y escalable para:

Catálogo maestro de sneakers

Tokens efímeros seguros

Funcionalidades críticas del ecosistema GOAT

Implementa Clean Architecture + DDD de manera pura, preparada para integrarse con Java + PostgreSQL y escalar a microservicios independientes sin refactor masivo.


---

✅ Ya tienes:
- Documento Java — identidad, navegación y listings  
- Documento Python — catálogo + tokens  

Si deseas, puedo ahora crear:

- 📊 Diagramas de arquitectura entre microservicios  
- 🧠 Mapas DDD  
- 🔄 Secuencias UML  
- 🗄️ ERD combinado Mongo + PostgreSQL  
- 📑 Plantillas README listas para GitHub  

Solo dime cuál quieres siguiente.
¡Quedó claro! Adoptamos **MongoDB con TTL** para **OTPs y password reset tokens** (efímeros, acceso rápido), y mantenemos **PostgreSQL** para identidad, roles/menú y listings. Abajo te dejo los **entregables en Markdown** listos para copiar/pegar:

---

# 📘 0. Decisiones clave (resumen)

* **OTPs → Mongo + TTL**: efímeros, alta concurrencia, borrado automático; se guarda **hash** del OTP (no el código plano).
* **Password reset tokens → Mongo + TTL**: *single-use* por diseño (si expiró o no existe, es inválido).
* **Email confirmation tokens → Mongo + TTL** (coherencia con tu preferencia; al confirmar, se actualiza `email_confirmed` en PG).
* **Catálogo y taxonomías (Shop público) → Mongo**: flexibles, media S3.
* **Identidad/RBAC/Menú dinámico/Listings → Postgres**: integridad, relaciones y reglas de edición/publicación.

---

# 📙 1. Documentación general de esquemas (MD)

## 1.1 PostgreSQL (schema‐first)

**Schemas**:

* `identity`: usuarios, roles, asignaciones.
* `navigation`: menús (públicos/privados) y relación con roles.
* `listing`: publicaciones del seller (precio fijo, tallas fijas, no editable si `PUBLISHED` → archivar y crear nueva).

**Entidades principales**

* `identity.users`

  * `id (uuid PK)` · `email (unique)` · `password_hash` · `email_confirmed` · `is_active` · `created_at`.
  * **Uso:** login seller/buyer, relación con roles.

* `identity.roles` & `identity.users_roles`

  * **RBAC**: SUPER_ADMIN, SELLER, BUYER, SUPPORT, CLIENT.
  * **Uso:** controlar acceso y construir menú dinámico.

* `navigation.menus` & `navigation.roles_menus`

  * Campos: `name`, `route`, `icon`, `menu_order`, `parent_id`, `is_public`.
  * **Uso:** árbol de navegación (público y por rol).

* `listing.listings`

  * `seller_id` (FK a `identity.users`) · `sneaker_sku` (ref a Mongo) · `size` · `condition` · `gender` · `color` · `price` · `status` (`DRAFT|PUBLISHED|ARCHIVED`) · `cover_image` (S3).
  * **Regla:** si está `PUBLISHED`, información vital **no se edita** → `ARCHIVED` + nuevo.

---

## 1.2 MongoDB (document-first)

**Bases sugeridas**:

* `auth`: `otps`, `password_resets`, `email_confirm` (todo con **TTL**).
* `catalog`: `sneakers`, `categories`, `collections`, `brands`.

**Colecciones clave**

* `auth.otps` (OTP efímero)

  * Campos: `email`, `purpose (REGISTER|LOGIN|EMAIL_VERIFY)`, `otpHash`, `attempts`, `createdAt`, `expireAt`.
  * Índices: TTL en `expireAt`, `{email, purpose}`.

* `auth.password_resets` (token de reseteo efímero)

  * Campos: `userId`, `tokenHash` (no token plano), `createdAt`, `expireAt`, `used (bool)` opcional.
  * Índices: TTL en `expireAt`, `tokenHash` único.

* `auth.email_confirm` (enlace de confirmación efímero)

  * Campos: `userId`, `tokenHash`, `createdAt`, `expireAt`, `used`.
  * Índices: TTL en `expireAt`, `tokenHash` único.

* `catalog.sneakers` (catálogo canónico)

  * Campos: `sku (unique)`, `brand`, `model`, `colorway?`, `gender`, `year?`, `sizes[] (fijo)`, `media{images[], cover}`, `attributes{}` flexible, `updatedAt`.
  * Índices: `sku` único, `{brand, model}`, `gender`.

* `catalog.categories` · `catalog.collections` · `catalog.brands`

  * Para **Shop público** (categorías, colecciones, marcas) con **slugs** únicos y flags `active`.

---

# 🐘 2. SQL listo para DataGrip (PostgreSQL)

> Pega esto tal cual en DataGrip. Crea extensiones, schemas y tablas con índices.

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========== identity ===========
CREATE SCHEMA IF NOT EXISTS identity;
SET search_path = identity, public;

CREATE TABLE IF NOT EXISTS users (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email           varchar(255) NOT NULL UNIQUE,
  password_hash   text NOT NULL,
  email_confirmed boolean NOT NULL DEFAULT false,
  is_active       boolean NOT NULL DEFAULT true,
  created_at      timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS roles (
  id    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  code  varchar(40) UNIQUE NOT NULL,   -- SUPER_ADMIN, SELLER, BUYER, SUPPORT, CLIENT
  name  varchar(80) NOT NULL
);

CREATE TABLE IF NOT EXISTS users_roles (
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_id uuid NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, role_id)
);

-- =========== navigation ===========
CREATE SCHEMA IF NOT EXISTS navigation;
SET search_path = navigation, public, identity;

CREATE TABLE IF NOT EXISTS menus (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  parent_id   uuid NULL REFERENCES menus(id) ON DELETE CASCADE,
  name        varchar(120) NOT NULL,
  route       varchar(160) NOT NULL,
  icon        varchar(80)  NULL,
  menu_order  int NOT NULL DEFAULT 0,
  is_public   boolean NOT NULL DEFAULT false,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_menus_parent ON menus(parent_id);
CREATE INDEX IF NOT EXISTS idx_menus_public ON menus(is_public);

CREATE TABLE IF NOT EXISTS roles_menus (
  role_id uuid NOT NULL REFERENCES identity.roles(id) ON DELETE CASCADE,
  menu_id uuid NOT NULL REFERENCES navigation.menus(id) ON DELETE CASCADE,
  PRIMARY KEY (role_id, menu_id)
);

-- =========== listing ===========
CREATE SCHEMA IF NOT EXISTS listing;
SET search_path = listing, public, identity;

DO $$ BEGIN
  CREATE TYPE listing_status AS ENUM ('DRAFT','PUBLISHED','ARCHIVED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS listings (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  seller_id     uuid NOT NULL REFERENCES identity.users(id),
  sneaker_sku   varchar(80) NOT NULL,        -- referencia a Mongo
  size          varchar(16) NOT NULL,        -- catálogo fijo de tallas
  condition     varchar(24) NOT NULL,        -- NEW, LIKE_NEW, USED_A...
  gender        varchar(16) NOT NULL,        -- MEN, WOMEN, GS, UNISEX
  brand         varchar(60) NOT NULL,        -- redundante para filtros rápidos
  color         varchar(40) NOT NULL,
  price         numeric(12,2) NOT NULL,      -- precio fijo
  status        listing_status NOT NULL DEFAULT 'DRAFT',
  cover_image   varchar(256) NULL,           -- S3 URL
  created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_listings_seller   ON listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_listings_status   ON listings(status);
CREATE INDEX IF NOT EXISTS idx_listings_browser  ON listings(sneaker_sku, size, status);
CREATE INDEX IF NOT EXISTS idx_listings_filters  ON listings(gender, brand, color);
```

**Notas de diseño**

* Redundamos `brand` en `listings` para filtrar sin “join” al catálogo (que está en Mongo).
* La **regla de no edición en `PUBLISHED`** se implementa en el servicio: si cambian campos vitales, se **archiva** y se crea una **nueva** fila.

---

# 🍃 3. MongoDB (validators + índices) — `mongosh`

> Pega por secciones en `mongosh`.

### 3.1 `auth` — OTPs y tokens (TTL)

```javascript
use auth;

/* OTPs efímeros */
db.createCollection("otps", {
  validator: {$jsonSchema:{
    bsonType:"object",
    required:["email","purpose","otpHash","createdAt","expireAt"],
    properties:{
      email:{bsonType:"string"},
      purpose:{enum:["REGISTER","LOGIN","EMAIL_VERIFY"]},
      otpHash:{bsonType:"string"},
      attempts:{bsonType:"int"},
      createdAt:{bsonType:"date"},
      expireAt:{bsonType:"date"}
    }
  }}
});
db.otps.createIndex({ expireAt: 1 }, { expireAfterSeconds: 0 });
db.otps.createIndex({ email: 1, purpose: 1 });

/* Password reset tokens efímeros (single-use por TTL + borrado al usar) */
db.createCollection("password_resets", {
  validator: {$jsonSchema:{
    bsonType:"object",
    required:["userId","tokenHash","createdAt","expireAt"],
    properties:{
      userId:{bsonType:"string"},       // UUID PG en string
      tokenHash:{bsonType:"string"},    // hash opaco
      used:{bsonType:["bool","null"]},
      createdAt:{bsonType:"date"},
      expireAt:{bsonType:"date"}
    }
  }}
});
db.password_resets.createIndex({ expireAt: 1 }, { expireAfterSeconds: 0 });
db.password_resets.createIndex({ tokenHash: 1 }, { unique: true });

/* Email confirm tokens efímeros */
db.createCollection("email_confirm", {
  validator: {$jsonSchema:{
    bsonType:"object",
    required:["userId","tokenHash","createdAt","expireAt"],
    properties:{
      userId:{bsonType:"string"},
      tokenHash:{bsonType:"string"},
      used:{bsonType:["bool","null"]},
      createdAt:{bsonType:"date"},
      expireAt:{bsonType:"date"}
    }
  }}
});
db.email_confirm.createIndex({ expireAt: 1 }, { expireAfterSeconds: 0 });
db.email_confirm.createIndex({ tokenHash: 1 }, { unique: true });
```

### 3.2 `catalog` — catálogo y taxonomías

```javascript
use catalog;

db.createCollection("sneakers", {
  validator: {$jsonSchema:{
    bsonType:"object",
    required:["sku","brand","model","sizes","media","updatedAt"],
    properties:{
      sku:{bsonType:"string"},
      brand:{bsonType:"string"},
      model:{bsonType:"string"},
      colorway:{bsonType:["string","null"]},
      gender:{enum:["MEN","WOMEN","GS","UNISEX", null]},
      year:{bsonType:["int","null"]},
      sizes:{bsonType:"array", items:{bsonType:"string"}, minItems:1},
      media:{bsonType:"object", required:["images"],
        properties:{ images:{bsonType:"array", items:{bsonType:"string"}}, cover:{bsonType:["string","null"]} }
      },
      attributes:{bsonType:["object","null"]},
      updatedAt:{bsonType:"date"}
    }
  }}
});
db.sneakers.createIndex({ sku: 1 }, { unique: true });
db.sneakers.createIndex({ brand: 1, model: 1 });
db.sneakers.createIndex({ gender: 1 });

db.createCollection("categories", {
  validator: {$jsonSchema:{
    bsonType:"object",
    required:["slug","name","level","active"],
    properties:{
      slug:{bsonType:"string"},
      name:{bsonType:"string"},
      parent:{bsonType:["string","null"]}, // slug padre
      level:{bsonType:"int"},              // 0=raíz
      active:{bsonType:"bool"}
    }
  }}
});
db.categories.createIndex({ slug: 1 }, { unique: true });
db.categories.createIndex({ parent: 1, level: 1 });

db.createCollection("collections", {
  validator: {$jsonSchema:{
    bsonType:"object",
    required:["slug","name","active"],
    properties:{
      slug:{bsonType:"string"},
      name:{bsonType:"string"},
      active:{bsonType:"bool"},
      sortOrder:{bsonType:["int","null"]}
    }
  }}
});
db.collections.createIndex({ slug: 1 }, { unique: true });
db.collections.createIndex({ active: 1, sortOrder: 1 });

db.createCollection("brands", {
  validator: {$jsonSchema:{
    bsonType:"object",
    required:["slug","name","active"],
    properties:{
      slug:{bsonType:"string"},
      name:{bsonType:"string"},
      active:{bsonType:"bool"}
    }
  }}
});
db.brands.createIndex({ slug: 1 }, { unique: true });
db.brands.createIndex({ active: 1 });
```

---

# 🐍 4. Mongo + Python (esquemas y repos) — FastAPI + Motor + Pydantic

> Modelos Pydantic (tipado), repos con **Motor** y creación de **índices/validadores**:

```python
# app/mongo/models.py
from pydantic import BaseModel, Field, EmailStr
from typing import List, Optional, Literal
from datetime import datetime

# --- AUTH ---
class OTPDoc(BaseModel):
    email: EmailStr
    purpose: Literal["REGISTER","LOGIN","EMAIL_VERIFY"]
    otpHash: str
    attempts: int = 0
    createdAt: datetime = Field(default_factory=datetime.utcnow)
    expireAt: datetime

class PasswordResetDoc(BaseModel):
    userId: str    # UUID de PG
    tokenHash: str
    used: bool = False
    createdAt: datetime = Field(default_factory=datetime.utcnow)
    expireAt: datetime

class EmailConfirmDoc(BaseModel):
    userId: str
    tokenHash: str
    used: bool = False
    createdAt: datetime = Field(default_factory=datetime.utcnow)
    expireAt: datetime

# --- CATALOG ---
class Media(BaseModel):
    images: List[str]
    cover: Optional[str] = None

class SneakerDoc(BaseModel):
    sku: str
    brand: str
    model: str
    colorway: Optional[str] = None
    gender: Optional[Literal["MEN","WOMEN","GS","UNISEX"]] = None
    year: Optional[int] = None
    sizes: List[str]
    media: Media
    attributes: Optional[dict] = None
    updatedAt: datetime = Field(default_factory=datetime.utcnow)
```

```python
# app/mongo/repo.py
from motor.motor_asyncio import AsyncIOMotorClient
from datetime import timedelta, datetime
from .models import OTPDoc, PasswordResetDoc, EmailConfirmDoc, SneakerDoc
import hashlib

def hash_token(raw: str, pepper: str) -> str:
    return hashlib.sha256((raw + pepper).encode()).hexdigest()

class MongoCtx:
    def __init__(self, uri: str):
        self.client = AsyncIOMotorClient(uri)
        self.auth = self.client["auth"]
        self.catalog = self.client["catalog"]

    async def ensure_indexes(self):
        # TTL
        await self.auth.otps.create_index("expireAt", expireAfterSeconds=0)
        await self.auth.otps.create_index([("email",1),("purpose",1)])

        await self.auth.password_resets.create_index("expireAt", expireAfterSeconds=0)
        await self.auth.password_resets.create_index("tokenHash", unique=True)

        await self.auth.email_confirm.create_index("expireAt", expireAfterSeconds=0)
        await self.auth.email_confirm.create_index("tokenHash", unique=True)

        await self.catalog.sneakers.create_index("sku", unique=True)
        await self.catalog.sneakers.create_index([("brand",1),("model",1)])
        await self.catalog.sneakers.create_index("gender")

    # --- OTP flows ---
    async def create_otp(self, email: str, purpose: str, otp_plain: str, ttl_seconds: int, pepper: str):
        doc = OTPDoc(
            email=email,
            purpose=purpose,
            otpHash=hash_token(otp_plain, pepper),
            expireAt=datetime.utcnow() + timedelta(seconds=ttl_seconds)
        ).model_dump()
        await self.auth.otps.insert_one(doc)

    async def verify_otp(self, email: str, purpose: str, otp_plain: str, pepper: str) -> bool:
        hashed = hash_token(otp_plain, pepper)
        doc = await self.auth.otps.find_one({"email": email, "purpose": purpose, "otpHash": hashed})
        if not doc: 
            return False
        # opcional: delete-on-use
        await self.auth.otps.delete_one({"_id": doc["_id"]})
        return True

    # --- Password reset ---
    async def create_reset(self, user_id: str, token_plain: str, ttl_seconds: int, pepper: str):
        doc = PasswordResetDoc(
            userId=user_id,
            tokenHash=hash_token(token_plain, pepper),
            expireAt=datetime.utcnow() + timedelta(seconds=ttl_seconds)
        ).model_dump()
        await self.auth.password_resets.insert_one(doc)

    async def consume_reset(self, token_plain: str, pepper: str) -> str | None:
        hashed = hash_token(token_plain, pepper)
        doc = await self.auth.password_resets.find_one({"tokenHash": hashed})
        if not doc:
            return None
        await self.auth.password_resets.delete_one({"_id": doc["_id"]})
        return doc["userId"]
```

---

# ☕ 5. Java (Spring Boot) — Postgres (JPA) + Mongo (Spring Data)

## 5.1 Postgres — entidades JPA (RBAC, Menú, Listings)

```java
// identity/User.java
@Entity @Table(name="users", schema="identity")
public class User {
  @Id @Column(columnDefinition="uuid") private UUID id;
  @Column(unique=true, nullable=false) private String email;
  @Column(nullable=false) private String passwordHash;
  private boolean emailConfirmed;
  private boolean isActive = true;
  @Column(nullable=false) private Instant createdAt = Instant.now();
}
```

```java
// identity/Role.java
@Entity @Table(name="roles", schema="identity")
public class Role {
  @Id @Column(columnDefinition="uuid") private UUID id;
  @Column(unique=true, nullable=false) private String code; // SELLER, BUYER...
  @Column(nullable=false) private String name;
}
```

```java
// identity/UserRole.java
@Entity @Table(name="users_roles", schema="identity")
@IdClass(UserRoleId.class)
public class UserRole {
  @Id @ManyToOne @JoinColumn(name="user_id") private User user;
  @Id @ManyToOne @JoinColumn(name="role_id") private Role role;
}
```

```java
// navigation/Menu.java
@Entity @Table(name="menus", schema="navigation")
public class Menu {
  @Id @Column(columnDefinition="uuid") private UUID id;
  private String name;
  private String route;
  private String icon;
  private Integer menuOrder;
  private Boolean isPublic;
  @ManyToOne @JoinColumn(name="parent_id") private Menu parent;
  private Instant createdAt;
  private Instant updatedAt;
}
```

```java
// navigation/RoleMenu.java
@Entity @Table(name="roles_menus", schema="navigation")
@IdClass(RoleMenuId.class)
public class RoleMenu {
  @Id @ManyToOne @JoinColumn(name="role_id") private Role role;
  @Id @ManyToOne @JoinColumn(name="menu_id") private Menu menu;
}
```

```java
// listing/Listing.java
@Entity @Table(name="listings", schema="listing")
public class Listing {
  @Id @Column(columnDefinition="uuid") private UUID id;
  @ManyToOne @JoinColumn(name="seller_id", nullable=false) private User seller;
  @Column(nullable=false) private String sneakerSku; // ref Mongo
  @Column(nullable=false) private String size;
  @Column(nullable=false) private String condition;
  @Column(nullable=false) private String gender;
  @Column(nullable=false) private String brand;
  @Column(nullable=false) private String color;
  @Column(nullable=false) private BigDecimal price;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private ListingStatus status;
  private String coverImage;
  private Instant createdAt;
}
```

**Repos (ejemplos)**

```java
public interface UserRepo extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);
}

public interface MenuRepo extends JpaRepository<Menu, UUID> {
  List<Menu> findByIsPublicTrueOrderByMenuOrderAsc();
}
```

## 5.2 Mongo — documentos y TTL (Spring Data Mongo)

```java
// auth/OtpDoc.java
@Document("otps")
@CompoundIndexes({
  @CompoundIndex(name="email_purpose_idx", def="{ 'email': 1, 'purpose': 1 }")
})
public class OtpDoc {
  @Id private String id;
  private String email;
  private String purpose;     // REGISTER | LOGIN | EMAIL_VERIFY
  private String otpHash;
  private Integer attempts = 0;
  private Date createdAt = new Date();
  @Indexed(name="ttl_expire_idx", expireAfterSeconds=0)
  private Date expireAt;
}
```

```java
// auth/PasswordResetDoc.java
@Document("password_resets")
public class PasswordResetDoc {
  @Id private String id;
  private String userId;  // UUID PG
  @Indexed(unique = true) private String tokenHash;
  private Boolean used = false;
  private Date createdAt = new Date();
  @Indexed(name="ttl_reset_idx", expireAfterSeconds=0)
  private Date expireAt;
}
```

```java
// auth/EmailConfirmDoc.java
@Document("email_confirm")
public class EmailConfirmDoc {
  @Id private String id;
  private String userId;
  @Indexed(unique = true) private String tokenHash;
  private Boolean used = false;
  private Date createdAt = new Date();
  @Indexed(name="ttl_confirm_idx", expireAfterSeconds=0)
  private Date expireAt;
}
```

```java
// catalog/SneakerDoc.java
@Document("sneakers")
@CompoundIndexes({
  @CompoundIndex(name="brand_model_idx", def="{ 'brand': 1, 'model': 1 }")
})
public class SneakerDoc {
  @Id private String id;       // opcional usar sku como _id
  @Indexed(unique = true) private String sku;
  private String brand;
  private String model;
  private String colorway;
  private String gender;       // MEN, WOMEN, GS, UNISEX
  private Integer year;
  private List<String> sizes;  // catálogo fijo
  private Map<String,Object> media;  // images[], cover
  private Map<String,Object> attributes;
  private Date updatedAt = new Date();
}
```

**Repos Mongo (ejemplos)**

```java
public interface OtpRepo extends MongoRepository<OtpDoc, String> {
  Optional<OtpDoc> findByEmailAndPurposeAndOtpHash(String email, String purpose, String otpHash);
}

public interface SneakerRepo extends MongoRepository<SneakerDoc, String> {
  Optional<SneakerDoc> findBySku(String sku);
  List<SneakerDoc> findByBrandAndModel(String brand, String model);
}
```

---

# 🧩 6. Queries/flows útiles

* **Menú público**: `SELECT * FROM navigation.menus WHERE is_public=true ORDER BY menu_order;`
* **Menú por rol**:

  ```sql
  SELECT m.* FROM navigation.menus m
  JOIN navigation.roles_menus rm ON rm.menu_id = m.id
  JOIN identity.users_roles ur ON ur.role_id = rm.role_id
  WHERE ur.user_id = :userId
  ORDER BY m.menu_order;
  ```
* **Sneakers (UI)**:

  1. Mongo: `find({ brand, gender })` → obtener `sku` y media.
  2. PG: `SELECT size, price FROM listing.listings WHERE sneaker_sku IN (...) AND status='PUBLISHED'`.

---

# ✅ 7. Cierre y siguientes pasos

* **OTPs** y **tokens** quedan **100% en Mongo con TTL** (rápidos y efímeros).
* **Identidad, RBAC, menús y listings** en **PostgreSQL** (sólido y claro).
* **Python** (Motor/Pydantic) y **Java** (Spring Data JPA/Mongo) listos para integrar.

¿Quieres que te entregue también un **docker-compose** (PG 16 + Mongo 7 + pgAdmin + Mongo Express) y **scripts de seed** (roles, menús públicos Shop/Categorías/Colecciones/Marcas) para que tengas todo corriendo local en 1 comando?

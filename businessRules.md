# Propósito del modelado de datos

## 1) Alinear el dominio con el producto (DDD)

* **Bounded Contexts** separados para evitar acoplamientos y facilitar la evolución:

  * **Identity/RBAC** (usuarios, roles, menús) — *PostgreSQL*.
  * **Catalog** (sneakers canónicos, marcas, categorías, colecciones, media) — *MongoDB* (flexible).
  * **Listings** (publicaciones del seller con precio/talla/condición) — *PostgreSQL*.
  * (Fase futura: **Orders/Payments/Verification/Shipping** — *PostgreSQL* resultado + *Mongo* evidencia).

## 2) Robustez + Velocidad donde corresponde

* **PostgreSQL** para relaciones fuertes e invariantes transaccionales (unicidades, estados, “una venta por listing” en F2).
* **MongoDB** para **contenido variable** y **lecturas rápidas** del catálogo (atributos, media, taxonomías) y **tokens efímeros** con TTL (OTPs, reset, confirm email).

## 3) UX/SEO/Operación

* **Menú dinámico** en BD: el backend **devuelve el árbol** según rol o público (guest). Permite A/B tests y cambios sin redeploy.
* **S3** para imágenes (bajo costo, distribución CDN), guardando en BD **solo URLs/keys**.
* **Escalabilidad**: consultas típicas de tienda (por talla, marca, condición, género, color) optimizadas con índices/denormalizaciones controladas.

---

# Reglas de negocio (MVP + visión GOAT)

## A) Cuentas y Acceso

1. **Registro Seller/Buyer con email + contraseña**.
2. **Verificación de correo** con **OTP** y/o **enlace** (ambos en **Mongo** con **TTL**).
3. **Guest access**: *Home* y *Shop* son públicos (sin login).
4. **Sin 2FA** (por ahora).
5. **Política de contraseñas** (mínimos de seguridad, reset por link TTL en Mongo).

**Invariantes**

* `email` único (PG).
* Link/OTP expiran por TTL (Mongo). Si “no existe”, no es válido.
* Un **usuario activo** puede tener **múltiples roles** (p. ej., SELLER y BUYER).

## B) Menú dinámico (RBAC + público)

6. **Menús y submenús** se definen en `navigation.menus` (PG), con `is_public` y jerarquía por `parent_id`.
7. **Visibilidad por rol** vía `roles_menus` (PG).
8. Menú **público**: *Home*, *Shop* y sus submenús **(Categorías, Colecciones, Marcas)**.
9. Menús **privados**: panel Seller, administración, etc.

**Invariantes**

* El **árbol** se arma solo con nodos permitidos por `is_public` o por **roles** del usuario autenticado.
* Orden estable por `menu_order`.

## C) Catálogo (Mongo) y Taxonomías

10. **`catalog.sneakers`** es el **canónico** (SKU único). Contiene `brand`, `model`, `gender`, `sizes[]` (fijas), `media.images/cover` (S3).
11. **Taxonomías**: `categories`, `collections`, `brands` con **slugs** únicos y `active: true/false`.
12. **Atributos flexibles** (e.g., materiales) sin migraciones de esquema.

**Invariantes**

* `sku` único; `sizes[]` debe provenir del **catálogo fijo** de tallas.
* `brand`/`model` coherentes con el merchandising (no se duplican con listings).

## D) Listings (PG) — publicaciones del Seller

13. Un **listing** referencia un `sneaker_sku` (Mongo) y define **talla**, **condición**, **género**, **color**, **precio fijo**, `status ∈ {DRAFT, PUBLISHED, ARCHIVED}`.
14. **Imágenes**: `cover_image` apunta a S3 (opcional más galería en F2).
15. **Edición restringida**: si `PUBLISHED`, **no** se puede modificar información vital (**talla, condición, color, precio, sku**). Se debe **ARCHIVE** y crear una **nueva publicación**.
16. **Filtrado**: por talla, marca, precio, condición, género, color (índices dedicados).

**Invariantes**

* `seller_id` debe ser un **usuario activo** con rol **SELLER**.
* Campos vitales bloqueados en `PUBLISHED`.
* Precios son **fijos** (sin “offers/bids” en MVP).

## E) (Visión GOAT para Fase 2)

17. **Compra con verificación** (*ship-to-verify*): se **autoriza** pago, seller envía al **centro de autenticación**, si **PASS** se **captura** y se envía al buyer; si **FAIL**, **refund** y retorno al seller.
18. **Payouts**: después de `CAPTURED`, se liquida al seller menos comisiones.
19. **Devoluciones/Protección**: ventana de reclamos, RMA guiado, evidencia fotográfica.

**Invariantes clave F2**

* **Una venta por listing** (FK `orders.listing_id UNIQUE`).
* `Order` 1:1 `Payment`, 1:1 `Verification`, 1..* `Shipments` (dos tramos).
* Eventos idempotentes (`order_placed`, `payment_authorized`, `verification_pass|fail`, `payment_captured|refunded`, `shipment_updated`, `order_completed`).

---

# Flujo esperado en la aplicación (end-to-end)

## 1) Onboarding y acceso

1. **Buyer/Seller abre la app** → ve **Home/Shop** (públicos).
2. **Registro**: email + password → se envía **OTP y/o enlace** (Mongo/TTL) → on click/OTP **confirma correo** y activa cuenta (PG: `email_confirmed=true`).
3. **Login**: email + password → JWT → backend entrega **menú** según rol; público siempre accesible.

## 2) Navegación (Shop)

4. Front consulta **Mongo** para construir submenús: **Categorías, Colecciones, Marcas**.
5. En **listado** (grid de sneakers):

   * **Mongo**: saca `sku`, `brand`, `model`, `media.cover`, `gender`.
   * **Postgres**: consulta `listings` `status='PUBLISHED'` para **tallas disponibles** y **precio**.
6. **Filtros**: talla, marca, precio, condición, género, color (con índices).
7. **Detalle**: ficha del sneaker (Mongo) + listings activos (PG).

## 3) Panel Seller (privado)

8. **Crear listing**: selecciona `sku` (autocompletado desde Mongo), define talla, condición, color, precio; sube **cover** a S3 → guarda **DRAFT** → **PUBLISH**.
9. **Editar listing**:

   * Si está `DRAFT`: edita todo.
   * Si está `PUBLISHED`: **no** se permite editar vital; si necesita cambio → **ARCHIVE** y **crear nuevo**.
10. **Mis listings**: tabla con filtros por estado, talla, precio, fecha.

## 4) (Fase 2) Checkout y verificación

11. **Buyer** elige un `listing` → **Create Order** (PG).
12. **Authorize** pago (hold en PSP).
13. **Seller → Centro** (Shipment #1).
14. **Verification**: PASS → **capture** y **Shipment #2** (Centro → Buyer); FAIL → **refund** + devoluciones.
15. **Completar**: entrega confirmada → **Order COMPLETED**; activar **payout** al seller.

---

# Datos que “cuidan” al negocio (invariantes críticos)

* **Autenticación & correo**:

  * `users.email` único (PG).
  * Tokens efímeros (OTP/reset/confirm) en **Mongo con TTL**; **hashes**, **nunca** valores planos.

* **Menú dinámico**:

  * Cada ruta del menú **siempre** se verifica por rol y/o `is_public`.
  * Cambios de navegación **no requieren** redeploy.

* **Listings**:

  * **No** se edita lo vital en `PUBLISHED`.
  * `brand` redundado en `listings` solo para **filtro rápido** (evitar lookup a Mongo); se mantiene congruencia con `sneaker_sku`.

* **(F2) Orden/Verificación/Pago**:

  * Patrón **Authorize → Verify → Capture/Refund**.
  * **Una venta por listing** (unicidad a nivel BD cuando se implemente Orders).
  * Trazabilidad de envíos (dos tramos).

---

# Qué ganarás con este modelado

* **Cambio ágil** en menú y merchandising sin tocar código.
* **Búsquedas rápidas** y ficha rica de producto (Mongo) + **consistencia** de publicaciones (PG).
* **Seguridad y UX**: OTPs y enlaces con TTL, sin fricción extra de 2FA.
* **Escalabilidad**: claros “hot paths” (catálogo y listados) y “strong paths” (identidad, publicaciones) con la base correcta para Fase 2 (compras/verificación).

---

Si quieres, convierto esto en:

* un **README.md** de arquitectura (resumen + diagramas de estados),
* un **BUSINESS_RULES.md** (invariantes y políticas),
* y un **FLOWS.md** con secuencias (Mermaid) de *Browse → View → Create Listing → Publish → (F2) Buy → Verify → Capture → Ship*.

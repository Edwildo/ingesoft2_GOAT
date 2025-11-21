-- ============================================================================
-- GOAT Project - PostgreSQL DDL
-- Database Schema for Java/Spring Boot
-- ============================================================================
--
-- Schemas:
--   - identity: Usuarios, roles y asignaciones (RBAC)
--   - navigation: Menús dinámicos públicos y por rol
--   - listing: Publicaciones de sellers (listings)
--
-- ============================================================================

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- SCHEMA: identity
-- ============================================================================
-- Usuarios, roles y control de acceso basado en roles (RBAC)

CREATE SCHEMA IF NOT EXISTS identity;
COMMENT ON SCHEMA identity IS 'Identity and RBAC: usuarios, roles y asignaciones';

-- ----------------------------------------------------------------------------
-- Tabla: users
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   TEXT NOT NULL,
    email_confirmed BOOLEAN NOT NULL DEFAULT false,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE identity.users IS 'Usuarios del sistema (sellers y buyers)';
COMMENT ON COLUMN identity.users.email IS 'Email único del usuario';
COMMENT ON COLUMN identity.users.password_hash IS 'Hash de la contraseña (bcrypt/argon2)';
COMMENT ON COLUMN identity.users.email_confirmed IS 'Indica si el email ha sido verificado';
COMMENT ON COLUMN identity.users.is_active IS 'Indica si la cuenta está activa';

-- Índices para users
CREATE INDEX IF NOT EXISTS idx_users_email ON identity.users(email);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON identity.users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_email_confirmed ON identity.users(email_confirmed);

-- Trigger para updated_at
CREATE OR REPLACE FUNCTION identity.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON identity.users
    FOR EACH ROW
    EXECUTE FUNCTION identity.update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Tabla: roles
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(40) NOT NULL UNIQUE,
    name        VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE identity.roles IS 'Roles del sistema (RBAC)';
COMMENT ON COLUMN identity.roles.code IS 'Código único del rol: SUPER_ADMIN, SELLER, BUYER, SUPPORT, CLIENT';
COMMENT ON COLUMN identity.roles.name IS 'Nombre descriptivo del rol';
COMMENT ON COLUMN identity.roles.description IS 'Descripción opcional del rol';

-- Índices para roles
CREATE UNIQUE INDEX IF NOT EXISTS idx_roles_code ON identity.roles(code);

-- ----------------------------------------------------------------------------
-- Tabla: users_roles (Many-to-Many)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.users_roles (
    user_id     UUID NOT NULL,
    role_id     UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id)
        REFERENCES identity.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id)
        REFERENCES identity.roles(id) ON DELETE CASCADE
);

COMMENT ON TABLE identity.users_roles IS 'Asignación de roles a usuarios (un usuario puede tener múltiples roles)';

-- Índices para users_roles
CREATE INDEX IF NOT EXISTS idx_users_roles_user_id ON identity.users_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_users_roles_role_id ON identity.users_roles(role_id);

-- ============================================================================
-- SCHEMA: navigation
-- ============================================================================
-- Menús dinámicos públicos y por rol

CREATE SCHEMA IF NOT EXISTS navigation;
COMMENT ON SCHEMA navigation IS 'Navegación dinámica: menús públicos y por rol';

-- ----------------------------------------------------------------------------
-- Tabla: menus
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS navigation.menus (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id   UUID NULL,
    name        VARCHAR(120) NOT NULL,
    route       VARCHAR(160) NOT NULL,
    icon        VARCHAR(80),
    menu_order  INTEGER NOT NULL DEFAULT 0,
    is_public   BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_menus_parent FOREIGN KEY (parent_id)
        REFERENCES navigation.menus(id) ON DELETE CASCADE
);

COMMENT ON TABLE navigation.menus IS 'Menús y submenús del sistema (árbol jerárquico)';
COMMENT ON COLUMN navigation.menus.parent_id IS 'ID del menú padre (NULL para raíz)';
COMMENT ON COLUMN navigation.menus.name IS 'Nombre del menú';
COMMENT ON COLUMN navigation.menus.route IS 'Ruta del frontend';
COMMENT ON COLUMN navigation.menus.icon IS 'Icono del menú (opcional)';
COMMENT ON COLUMN navigation.menus.menu_order IS 'Orden de visualización';
COMMENT ON COLUMN navigation.menus.is_public IS 'Indica si el menú es público (accesible sin autenticación)';

-- Índices para menus
CREATE INDEX IF NOT EXISTS idx_menus_parent_id ON navigation.menus(parent_id);
CREATE INDEX IF NOT EXISTS idx_menus_is_public ON navigation.menus(is_public);
CREATE INDEX IF NOT EXISTS idx_menus_menu_order ON navigation.menus(menu_order);
CREATE INDEX IF NOT EXISTS idx_menus_route ON navigation.menus(route);

-- Trigger para updated_at
CREATE TRIGGER trigger_menus_updated_at
    BEFORE UPDATE ON navigation.menus
    FOR EACH ROW
    EXECUTE FUNCTION identity.update_updated_at_column();

-- ----------------------------------------------------------------------------
-- Tabla: roles_menus (Many-to-Many)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS navigation.roles_menus (
    role_id     UUID NOT NULL,
    menu_id     UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_roles_menus_role FOREIGN KEY (role_id)
        REFERENCES identity.roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_roles_menus_menu FOREIGN KEY (menu_id)
        REFERENCES navigation.menus(id) ON DELETE CASCADE
);

COMMENT ON TABLE navigation.roles_menus IS 'Asignación de menús a roles (control de visibilidad por rol)';

-- Índices para roles_menus
CREATE INDEX IF NOT EXISTS idx_roles_menus_role_id ON navigation.roles_menus(role_id);
CREATE INDEX IF NOT EXISTS idx_roles_menus_menu_id ON navigation.roles_menus(menu_id);

-- ============================================================================
-- SCHEMA: listing
-- ============================================================================
-- Publicaciones de sellers (listings de sneakers)

CREATE SCHEMA IF NOT EXISTS listing;
COMMENT ON SCHEMA listing IS 'Listings: publicaciones de sellers con precio, talla y condición';

-- ----------------------------------------------------------------------------
-- Tipo ENUM: listing_status
-- ----------------------------------------------------------------------------
DO $$ BEGIN
    CREATE TYPE listing.listing_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

COMMENT ON TYPE listing.listing_status IS 'Estados de un listing: DRAFT (borrador), PUBLISHED (publicado), ARCHIVED (archivado)';

-- ----------------------------------------------------------------------------
-- Tabla: listings
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS listing.listings (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id     UUID NOT NULL,
    sneaker_sku   VARCHAR(80) NOT NULL,
    size          VARCHAR(16) NOT NULL,
    condition     VARCHAR(24) NOT NULL,
    gender        VARCHAR(16) NOT NULL,
    brand         VARCHAR(60) NOT NULL,
    color         VARCHAR(40) NOT NULL,
    price         NUMERIC(12, 2) NOT NULL,
    status        listing.listing_status NOT NULL DEFAULT 'DRAFT',
    cover_image   VARCHAR(256),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listings_seller FOREIGN KEY (seller_id)
        REFERENCES identity.users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_listings_price_positive CHECK (price > 0)
);

COMMENT ON TABLE listing.listings IS 'Publicaciones de sellers (listings de sneakers)';
COMMENT ON COLUMN listing.listings.seller_id IS 'Usuario seller que creó el listing';
COMMENT ON COLUMN listing.listings.sneaker_sku IS 'SKU del sneaker en MongoDB (catálogo canónico)';
COMMENT ON COLUMN listing.listings.size IS 'Talla del sneaker (catálogo fijo de tallas)';
COMMENT ON COLUMN listing.listings.condition IS 'Condición: NEW, LIKE_NEW, USED_A, USED_B, etc.';
COMMENT ON COLUMN listing.listings.gender IS 'Género: MEN, WOMEN, GS, UNISEX';
COMMENT ON COLUMN listing.listings.brand IS 'Marca (redundante para filtros rápidos, evita lookup a Mongo)';
COMMENT ON COLUMN listing.listings.color IS 'Color del sneaker';
COMMENT ON COLUMN listing.listings.price IS 'Precio fijo (sin ofertas/bids en MVP)';
COMMENT ON COLUMN listing.listings.status IS 'Estado del listing (DRAFT/PUBLISHED/ARCHIVED)';
COMMENT ON COLUMN listing.listings.cover_image IS 'URL de la imagen de portada en S3';

-- Índices para listings
CREATE INDEX IF NOT EXISTS idx_listings_seller_id ON listing.listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_listings_status ON listing.listings(status);
CREATE INDEX IF NOT EXISTS idx_listings_sneaker_sku ON listing.listings(sneaker_sku);
CREATE INDEX IF NOT EXISTS idx_listings_browser ON listing.listings(sneaker_sku, size, status)
    WHERE status = 'PUBLISHED';
CREATE INDEX IF NOT EXISTS idx_listings_filters ON listing.listings(gender, brand, color, status)
    WHERE status = 'PUBLISHED';
CREATE INDEX IF NOT EXISTS idx_listings_price ON listing.listings(price)
    WHERE status = 'PUBLISHED';
CREATE INDEX IF NOT EXISTS idx_listings_created_at ON listing.listings(created_at DESC);

-- Trigger para updated_at
CREATE TRIGGER trigger_listings_updated_at
    BEFORE UPDATE ON listing.listings
    FOR EACH ROW
    EXECUTE FUNCTION identity.update_updated_at_column();

-- ============================================================================
-- DATOS INICIALES (Seed Data)
-- ============================================================================

-- Roles base del sistema
INSERT INTO identity.roles (code, name, description) VALUES
    ('SUPER_ADMIN', 'Super Administrador', 'Acceso completo al sistema'),
    ('SELLER', 'Vendedor', 'Puede crear y gestionar listings'),
    ('BUYER', 'Comprador', 'Puede comprar productos'),
    ('SUPPORT', 'Soporte', 'Atención al cliente'),
    ('CLIENT', 'Cliente', 'Rol genérico de cliente')
ON CONFLICT (code) DO NOTHING;

-- Menús públicos base (Home y Shop)
INSERT INTO navigation.menus (name, route, icon, menu_order, is_public) VALUES
    ('Home', '/', 'home', 1, true),
    ('Shop', '/shop', 'shop', 2, true)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- VISTAS ÚTILES (Opcional, para consultas comunes)
-- ============================================================================

-- Vista: usuarios con sus roles
CREATE OR REPLACE VIEW identity.vw_users_with_roles AS
SELECT
    u.id,
    u.email,
    u.email_confirmed,
    u.is_active,
    u.created_at,
    u.updated_at,
    ARRAY_AGG(r.code) FILTER (WHERE r.code IS NOT NULL) AS roles
FROM identity.users u
LEFT JOIN identity.users_roles ur ON u.id = ur.user_id
LEFT JOIN identity.roles r ON ur.role_id = r.id
GROUP BY u.id, u.email, u.email_confirmed, u.is_active, u.created_at, u.updated_at;

COMMENT ON VIEW identity.vw_users_with_roles IS 'Vista de usuarios con sus roles agregados como array';

-- Vista: menús públicos y por rol
CREATE OR REPLACE VIEW navigation.vw_menus_by_role AS
SELECT
    m.id,
    m.parent_id,
    m.name,
    m.route,
    m.icon,
    m.menu_order,
    m.is_public,
    ARRAY_AGG(DISTINCT r.code) FILTER (WHERE r.code IS NOT NULL) AS allowed_roles
FROM navigation.menus m
LEFT JOIN navigation.roles_menus rm ON m.id = rm.menu_id
LEFT JOIN identity.roles r ON rm.role_id = r.id
GROUP BY m.id, m.parent_id, m.name, m.route, m.icon, m.menu_order, m.is_public;

COMMENT ON VIEW navigation.vw_menus_by_role IS 'Vista de menús con roles permitidos agregados como array';

-- Vista: listings publicados con información del seller
CREATE OR REPLACE VIEW listing.vw_published_listings AS
SELECT
    l.id,
    l.sneaker_sku,
    l.size,
    l.condition,
    l.gender,
    l.brand,
    l.color,
    l.price,
    l.cover_image,
    l.created_at,
    u.email AS seller_email,
    u.id AS seller_id
FROM listing.listings l
INNER JOIN identity.users u ON l.seller_id = u.id
WHERE l.status = 'PUBLISHED' AND u.is_active = true;

COMMENT ON VIEW listing.vw_published_listings IS 'Vista de listings publicados con información del seller';

-- ============================================================================
-- GRANTS (Ajustar según usuario de aplicación)
-- ============================================================================
-- Ejemplo: GRANT USAGE ON SCHEMA identity, navigation, listing TO goat_app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA identity, navigation, listing TO goat_app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA identity, navigation, listing TO goat_app_user;

-- ============================================================================
-- FIN DEL DDL
-- ============================================================================


DELETE from identity.users where users.email = 'edwinpinilla125@gmail.com'
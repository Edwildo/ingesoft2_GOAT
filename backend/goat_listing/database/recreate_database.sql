-- ============================================================================
-- Script para RECREAR la base de datos completa desde cero
-- Elimina todo y lo recrea con status como VARCHAR en lugar de ENUM
-- ============================================================================

-- ADVERTENCIA: Este script ELIMINA TODOS LOS DATOS
-- Solo ejecuta esto si estás seguro de que quieres empezar de cero

BEGIN;

-- ============================================================================
-- PASO 1: Eliminar todo lo existente
-- ============================================================================

-- Eliminar vistas primero (tienen dependencias)
DROP VIEW IF EXISTS listing.vw_published_listings CASCADE;
DROP VIEW IF EXISTS navigation.vw_menus_by_role CASCADE;
DROP VIEW IF EXISTS identity.vw_users_with_roles CASCADE;

-- Eliminar tablas
DROP TABLE IF EXISTS listing.listings CASCADE;
DROP TABLE IF EXISTS navigation.roles_menus CASCADE;
DROP TABLE IF EXISTS navigation.menus CASCADE;
DROP TABLE IF EXISTS identity.users_roles CASCADE;
DROP TABLE IF EXISTS identity.roles CASCADE;
DROP TABLE IF EXISTS identity.users CASCADE;

-- Eliminar tipos ENUM
DROP TYPE IF EXISTS listing.listing_status CASCADE;

-- Eliminar schemas
DROP SCHEMA IF EXISTS listing CASCADE;
DROP SCHEMA IF EXISTS navigation CASCADE;
DROP SCHEMA IF EXISTS identity CASCADE;

-- Eliminar funciones
DROP FUNCTION IF EXISTS identity.update_updated_at_column() CASCADE;

COMMIT;

-- ============================================================================
-- PASO 2: Crear todo desde cero
-- ============================================================================

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- SCHEMA: identity
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS identity;
COMMENT ON SCHEMA identity IS 'Identity and RBAC: usuarios, roles y asignaciones';

-- Función para updated_at
CREATE OR REPLACE FUNCTION identity.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tabla: users
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

-- Índices para users
CREATE INDEX IF NOT EXISTS idx_users_email ON identity.users(email);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON identity.users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_email_confirmed ON identity.users(email_confirmed);

-- Trigger para updated_at
CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON identity.users
    FOR EACH ROW
    EXECUTE FUNCTION identity.update_updated_at_column();

-- Tabla: roles
CREATE TABLE IF NOT EXISTS identity.roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(40) NOT NULL UNIQUE,
    name        VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE identity.roles IS 'Roles del sistema (RBAC)';

CREATE UNIQUE INDEX IF NOT EXISTS idx_roles_code ON identity.roles(code);

-- Tabla: users_roles
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

CREATE INDEX IF NOT EXISTS idx_users_roles_user_id ON identity.users_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_users_roles_role_id ON identity.users_roles(role_id);

-- ============================================================================
-- SCHEMA: navigation
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS navigation;
COMMENT ON SCHEMA navigation IS 'Navegación dinámica: menús públicos y por rol';

-- Tabla: menus
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

CREATE INDEX IF NOT EXISTS idx_menus_parent_id ON navigation.menus(parent_id);
CREATE INDEX IF NOT EXISTS idx_menus_is_public ON navigation.menus(is_public);
CREATE INDEX IF NOT EXISTS idx_menus_menu_order ON navigation.menus(menu_order);
CREATE INDEX IF NOT EXISTS idx_menus_route ON navigation.menus(route);

CREATE TRIGGER trigger_menus_updated_at
    BEFORE UPDATE ON navigation.menus
    FOR EACH ROW
    EXECUTE FUNCTION identity.update_updated_at_column();

-- Tabla: roles_menus
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

CREATE INDEX IF NOT EXISTS idx_roles_menus_role_id ON navigation.roles_menus(role_id);
CREATE INDEX IF NOT EXISTS idx_roles_menus_menu_id ON navigation.roles_menus(menu_id);

-- ============================================================================
-- SCHEMA: listing
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS listing;
COMMENT ON SCHEMA listing IS 'Listings: publicaciones de sellers con precio, talla y condición';

-- Tabla: listings
-- IMPORTANTE: status es VARCHAR en lugar de ENUM
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
    status        VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    cover_image   VARCHAR(256),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listings_seller FOREIGN KEY (seller_id)
        REFERENCES identity.users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_listings_price_positive CHECK (price > 0),
    CONSTRAINT check_listing_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

COMMENT ON TABLE listing.listings IS 'Publicaciones de sellers (listings de sneakers)';
COMMENT ON COLUMN listing.listings.status IS 'Estado del listing (DRAFT/PUBLISHED/ARCHIVED) - VARCHAR con CHECK constraint';

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

-- Menús públicos base
INSERT INTO navigation.menus (name, route, icon, menu_order, is_public) VALUES
    ('Home', '/', 'home', 1, true),
    ('Shop', '/shop', 'shop', 2, true)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- VISTAS ÚTILES
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
-- IMPORTANTE: Sin cast explícito porque status es VARCHAR
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
-- VERIFICACIÓN FINAL
-- ============================================================================

SELECT 
    'VERIFICACIÓN - Columna status:' as info,
    column_name, 
    data_type,
    udt_name,
    character_maximum_length
FROM information_schema.columns 
WHERE table_schema = 'listing' 
  AND table_name = 'listings' 
  AND column_name = 'status';
-- Debe mostrar: data_type = 'character varying', udt_name = 'varchar'

SELECT 
    'VERIFICACIÓN - Constraint:' as info,
    constraint_name, 
    check_clause
FROM information_schema.check_constraints
WHERE constraint_name = 'check_listing_status';

SELECT 
    'VERIFICACIÓN - Vista:' as info,
    COUNT(*) as existe
FROM information_schema.views
WHERE table_schema = 'listing' 
  AND table_name = 'vw_published_listings';

SELECT 
    'VERIFICACIÓN - Roles creados:' as info,
    COUNT(*) as total_roles
FROM identity.roles;

SELECT 
    'VERIFICACIÓN - Menús creados:' as info,
    COUNT(*) as total_menus
FROM navigation.menus;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================


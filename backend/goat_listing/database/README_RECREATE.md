# Script de Recreación de Base de Datos

## ⚠️ ADVERTENCIA

Este script **ELIMINA TODOS LOS DATOS** de la base de datos y la recrea desde cero.

**Solo ejecuta esto si:**
- Estás en desarrollo
- No tienes datos importantes que perder
- Quieres empezar completamente limpio

## 📋 Qué hace el script

1. **Elimina todo:**
   - Todas las vistas
   - Todas las tablas
   - El tipo ENUM `listing.listing_status`
   - Los schemas (identity, navigation, listing)
   - Las funciones

2. **Recrea todo desde cero:**
   - Todos los schemas
   - Todas las tablas
   - **La columna `status` es VARCHAR(20) con CHECK constraint** (no ENUM)
   - Todas las vistas (sin cast explícito)
   - Funciones y triggers
   - Datos iniciales (roles y menús)

## 🚀 Cómo ejecutar

### Opción 1: Desde psql (línea de comandos)
```bash
psql -U postgres -d postgres -f database/recreate_database.sql
```

### Opción 2: Desde un cliente SQL (pgAdmin, DBeaver, etc.)
1. Abre tu cliente SQL
2. Conéctate a la base de datos `postgres`
3. Abre el archivo `database/recreate_database.sql`
4. Ejecuta todo el script

### Opción 3: Desde psql interactivo
```bash
psql -U postgres -d postgres
```
Luego:
```sql
\i database/recreate_database.sql
```

## ✅ Después de ejecutar

1. **Verifica que todo se creó correctamente:**
   El script incluye verificaciones al final que muestran:
   - Tipo de columna `status` (debe ser `character varying`)
   - Constraint creado
   - Vista creada
   - Roles y menús iniciales

2. **Reinicia tu aplicación Java:**
   ```bash
   mvn spring-boot:run
   ```

3. **Prueba crear un listing:**
   Debería funcionar sin errores de casting.

## 🔍 Cambios principales

### Antes (con ENUM):
```sql
CREATE TYPE listing.listing_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
status listing.listing_status NOT NULL DEFAULT 'DRAFT'
```

### Ahora (con VARCHAR + CHECK):
```sql
status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
CONSTRAINT check_listing_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
```

## 📝 Notas

- **Todos los datos se pierden** - El script elimina todo
- **Los roles y menús iniciales se recrean** automáticamente
- **La vista se crea sin cast explícito** porque ahora es VARCHAR
- **La entidad Java ya está configurada correctamente** con `length = 20`

## 🐛 Si algo falla

Si el script falla en algún punto:
1. Revisa los mensajes de error
2. Verifica que tengas permisos para eliminar schemas
3. Asegúrate de que no haya conexiones activas a la base de datos
4. Intenta ejecutar los comandos manualmente paso a paso


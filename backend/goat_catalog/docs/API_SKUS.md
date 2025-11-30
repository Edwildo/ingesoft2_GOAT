# API de SKUs (Sneakers) - Documentación Frontend

Documentación de los endpoints para gestionar SKUs (sneakers) en el catálogo canónico.

**Base URL:** `http://localhost:8082/api/catalog`

---

## 📋 Endpoints Disponibles

### 1. Obtener Sneaker por SKU

**Endpoint:** `GET /api/catalog/sneakers/{sku}`

**Descripción:** Obtiene un sneaker específico por su SKU. Endpoint **CRÍTICO** para validar que un SKU existe antes de crear un listing.

**Parámetros:**
- `sku` (path): SKU único del sneaker (ej: `NIKE-AIR-JORDAN-1-001`)

**Respuestas:**

✅ **200 OK** - Sneaker encontrado
```json
{
  "sku": "NIKE-AIR-JORDAN-1-001",
  "brand": "nike",
  "model": "Air Jordan 1",
  "gender": "UNISEX",
  "description": "Classic basketball sneaker",
  "categories": ["basketball", "sports"],
  "collections": ["jordan-collection"],
  "media": {
    "cover_image": "https://example.com/cover.jpg",
    "gallery": [
      "https://example.com/img1.jpg",
      "https://example.com/img2.jpg"
    ]
  }
}
```

❌ **404 Not Found** - SKU no existe
```json
{
  "detail": "Sneaker con SKU 'NIKE-AIR-JORDAN-1-001' no encontrado en el catálogo"
}
```

❌ **400 Bad Request** - SKU inválido
```json
{
  "detail": "SKU inválido. Solo se permiten letras mayúsculas, números, guiones y guiones bajos: abc"
}
```

**Ejemplo de uso (JavaScript/TypeScript):**

```typescript
async function getSneakerBySku(sku: string) {
  try {
    const response = await fetch(`http://localhost:8082/api/catalog/sneakers/${sku}`);
    
    if (response.status === 404) {
      console.log(`SKU ${sku} no existe en el catálogo`);
      return null;
    }
    
    if (!response.ok) {
      throw new Error(`Error: ${response.status}`);
    }
    
    const sneaker = await response.json();
    return sneaker;
  } catch (error) {
    console.error('Error al obtener sneaker:', error);
    throw error;
  }
}

// Uso:
const sneaker = await getSneakerBySku('NIKE-AIR-JORDAN-1-001');
if (sneaker) {
  console.log('Sneaker encontrado:', sneaker.model);
}
```

**Ejemplo de uso (React Hook):**

```typescript
import { useState, useEffect } from 'react';

function useSneakerBySku(sku: string | null) {
  const [sneaker, setSneaker] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!sku) {
      setSneaker(null);
      return;
    }

    setLoading(true);
    setError(null);

    fetch(`http://localhost:8082/api/catalog/sneakers/${sku}`)
      .then(async (res) => {
        if (res.status === 404) {
          setSneaker(null);
          return;
        }
        if (!res.ok) {
          const errorData = await res.json();
          throw new Error(errorData.detail || 'Error al obtener sneaker');
        }
        return res.json();
      })
      .then((data) => {
        setSneaker(data);
      })
      .catch((err) => {
        setError(err.message);
        setSneaker(null);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [sku]);

  return { sneaker, loading, error };
}

// Uso en componente:
function SneakerDetails({ sku }: { sku: string }) {
  const { sneaker, loading, error } = useSneakerBySku(sku);

  if (loading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!sneaker) return <div>SKU no encontrado</div>;

  return (
    <div>
      <h2>{sneaker.model}</h2>
      <p>Marca: {sneaker.brand}</p>
      <p>Género: {sneaker.gender}</p>
      {sneaker.media.cover_image && (
        <img src={sneaker.media.cover_image} alt={sneaker.model} />
      )}
    </div>
  );
}
```

---

### 2. Crear Nuevo Sneaker

**Endpoint:** `POST /api/catalog/sneakers`

**Descripción:** Crea un nuevo sneaker en el catálogo. Usado cuando un usuario quiere crear un listing con un SKU que aún no existe en el catálogo.

**Request Body:**
```json
{
  "sku": "NIKE-AIR-JORDAN-1-001",
  "brand": "nike",
  "model": "Air Jordan 1",
  "gender": "UNISEX",
  "description": "Classic basketball sneaker",
  "categories": ["basketball", "sports"],
  "collections": ["jordan-collection"],
  "media": {
    "cover_image": "https://example.com/cover.jpg",
    "gallery": [
      "https://example.com/img1.jpg",
      "https://example.com/img2.jpg"
    ]
  }
}
```

**Campos requeridos:**
- `sku` (string): SKU único (3-50 caracteres, alfanumérico, guiones y guiones bajos)
- `brand` (string): ID o nombre de la marca
- `model` (string): Modelo del sneaker
- `gender` (string): `"MALE"`, `"FEMALE"` o `"UNISEX"`

**Campos opcionales:**
- `description` (string): Descripción del sneaker
- `categories` (string[]): Array de IDs de categorías
- `collections` (string[]): Array de IDs de colecciones
- `media` (object): Información de medios
  - `cover_image` (string): URL de imagen de portada
  - `gallery` (string[]): Array de URLs de imágenes adicionales

**Respuestas:**

✅ **201 Created** - Sneaker creado exitosamente
```json
{
  "sku": "NIKE-AIR-JORDAN-1-001",
  "brand": "nike",
  "model": "Air Jordan 1",
  "gender": "UNISEX",
  "description": "Classic basketball sneaker",
  "categories": ["basketball"],
  "collections": ["jordan-collection"],
  "media": {
    "cover_image": "https://example.com/cover.jpg",
    "gallery": ["https://example.com/img1.jpg"]
  }
}
```

❌ **409 Conflict** - SKU ya existe
```json
{
  "detail": "Ya existe un sneaker con SKU 'NIKE-AIR-JORDAN-1-001' en el catálogo"
}
```

❌ **400 Bad Request** - Datos inválidos
```json
{
  "detail": "SKU inválido. Solo se permiten letras mayúsculas, números, guiones y guiones bajos: abc"
}
```

**Ejemplo de uso (JavaScript/TypeScript):**

```typescript
interface CreateSneakerRequest {
  sku: string;
  brand: string;
  model: string;
  gender: 'MALE' | 'FEMALE' | 'UNISEX';
  description?: string;
  categories?: string[];
  collections?: string[];
  media?: {
    cover_image?: string;
    gallery?: string[];
  };
}

async function createSneaker(data: CreateSneakerRequest) {
  try {
    const response = await fetch('http://localhost:8082/api/catalog/sneakers', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });

    if (response.status === 409) {
      const error = await response.json();
      throw new Error(`SKU ya existe: ${error.detail}`);
    }

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.detail || 'Error al crear sneaker');
    }

    const sneaker = await response.json();
    return sneaker;
  } catch (error) {
    console.error('Error al crear sneaker:', error);
    throw error;
  }
}

// Uso:
const newSneaker = await createSneaker({
  sku: 'NIKE-AIR-JORDAN-1-001',
  brand: 'nike',
  model: 'Air Jordan 1',
  gender: 'UNISEX',
  description: 'Classic basketball sneaker',
  categories: ['basketball'],
  media: {
    cover_image: 'https://example.com/cover.jpg',
  },
});

console.log('Sneaker creado:', newSneaker);
```

**Ejemplo de uso (React Hook):**

```typescript
import { useState } from 'react';

function useCreateSneaker() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createSneaker = async (data: CreateSneakerRequest) => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch('http://localhost:8082/api/catalog/sneakers', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
      });

      if (response.status === 409) {
        const errorData = await response.json();
        throw new Error(errorData.detail);
      }

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.detail || 'Error al crear sneaker');
      }

      const sneaker = await response.json();
      return sneaker;
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { createSneaker, loading, error };
}

// Uso en componente:
function CreateListingForm() {
  const { createSneaker, loading, error } = useCreateSneaker();
  const [sku, setSku] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      // Primero verificar si existe
      const checkResponse = await fetch(`http://localhost:8082/api/catalog/sneakers/${sku}`);
      
      if (checkResponse.status === 404) {
        // No existe, crear uno nuevo
        await createSneaker({
          sku,
          brand: 'nike', // Obtener del formulario
          model: 'Air Jordan 1', // Obtener del formulario
          gender: 'UNISEX', // Obtener del formulario
        });
        alert('Sneaker creado exitosamente');
      } else {
        alert('SKU ya existe en el catálogo');
      }
    } catch (err: any) {
      alert(`Error: ${err.message}`);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        value={sku}
        onChange={(e) => setSku(e.target.value.toUpperCase())}
        placeholder="SKU (ej: NIKE-AIR-JORDAN-1-001)"
      />
      <button type="submit" disabled={loading}>
        {loading ? 'Creando...' : 'Crear Sneaker'}
      </button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </form>
  );
}
```

---

### 3. Buscar Sneakers

**Endpoint:** `GET /api/catalog/sneakers`

**Descripción:** Busca sneakers con filtros opcionales. Útil para autocompletado y búsqueda en el frontend.

**Query Parameters:**
- `brand` (string, opcional): Filtrar por marca (ID o nombre)
- `category` (string, opcional): Filtrar por categoría (ID o slug)
- `gender` (string, opcional): Filtrar por género (`MALE`, `FEMALE`, `UNISEX`)
- `collection` (string, opcional): Filtrar por colección (ID o slug)
- `search` (string, opcional): Texto de búsqueda (busca en modelo y descripción)
- `page` (number, opcional): Número de página (default: 1)
- `size` (number, opcional): Tamaño de página (default: 20, máximo: 100)

**Respuesta:**

✅ **200 OK**
```json
{
  "sneakers": [
    {
      "sku": "NIKE-AIR-JORDAN-1-001",
      "brand": "nike",
      "model": "Air Jordan 1",
      "gender": "UNISEX",
      "description": "Classic basketball sneaker",
      "categories": ["basketball"],
      "collections": ["jordan-collection"],
      "media": {
        "cover_image": "https://example.com/cover.jpg",
        "gallery": []
      }
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

**Ejemplo de uso (Autocompletado):**

```typescript
async function searchSneakers(searchText: string) {
  try {
    const params = new URLSearchParams({
      search: searchText,
      size: '10', // Limitar resultados para autocompletado
    });

    const response = await fetch(
      `http://localhost:8082/api/catalog/sneakers?${params}`
    );

    if (!response.ok) {
      throw new Error(`Error: ${response.status}`);
    }

    const data = await response.json();
    return data.sneakers;
  } catch (error) {
    console.error('Error al buscar sneakers:', error);
    return [];
  }
}

// Uso en componente de búsqueda:
function SneakerSearchInput() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);

  useEffect(() => {
    if (query.length < 3) {
      setResults([]);
      return;
    }

    const timer = setTimeout(() => {
      searchSneakers(query).then(setResults);
    }, 300); // Debounce

    return () => clearTimeout(timer);
  }, [query]);

  return (
    <div>
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Buscar sneakers..."
      />
      {results.length > 0 && (
        <ul>
          {results.map((sneaker) => (
            <li key={sneaker.sku}>
              {sneaker.model} ({sneaker.sku})
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
```

---

## 🔄 Flujo Recomendado para Crear Listings

### Opción 1: Validar y Crear SKU si no existe

```typescript
async function ensureSneakerExists(sku: string, sneakerData: CreateSneakerRequest) {
  try {
    // 1. Verificar si el SKU existe
    const existing = await fetch(`http://localhost:8082/api/catalog/sneakers/${sku}`);
    
    if (existing.ok) {
      // SKU ya existe, continuar con la creación del listing
      console.log('SKU existe en el catálogo');
      return await existing.json();
    }
    
    if (existing.status === 404) {
      // SKU no existe, crearlo primero
      console.log('SKU no existe, creando...');
      const newSneaker = await createSneaker(sneakerData);
      console.log('SKU creado exitosamente');
      return newSneaker;
    }
    
    throw new Error(`Error inesperado: ${existing.status}`);
  } catch (error) {
    console.error('Error al asegurar que el SKU existe:', error);
    throw error;
  }
}

// Uso en el flujo de creación de listing:
async function createListing(listingData: ListingData) {
  // Asegurar que el SKU existe
  const sneaker = await ensureSneakerExists(listingData.sku, {
    sku: listingData.sku,
    brand: listingData.brand,
    model: listingData.model,
    gender: listingData.gender,
  });
  
  // Ahora crear el listing en el backend Java
  // ...
}
```

### Opción 2: Intentar crear directamente (más simple)

```typescript
async function createSneakerIfNotExists(sneakerData: CreateSneakerRequest) {
  try {
    const response = await fetch('http://localhost:8082/api/catalog/sneakers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(sneakerData),
    });

    if (response.status === 409) {
      // SKU ya existe, obtener el existente
      console.log('SKU ya existe, obteniendo...');
      const existing = await fetch(
        `http://localhost:8082/api/catalog/sneakers/${sneakerData.sku}`
      );
      return await existing.json();
    }

    if (!response.ok) {
      throw new Error(`Error: ${response.status}`);
    }

    // SKU creado exitosamente
    return await response.json();
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}
```

---

## 📝 Validaciones Importantes

### SKU
- **Formato:** Solo letras mayúsculas, números, guiones (`-`) y guiones bajos (`_`)
- **Longitud:** Mínimo 3 caracteres, máximo 50 caracteres
- **Ejemplos válidos:**
  - ✅ `NIKE-AIR-JORDAN-1-001`
  - ✅ `ADIDAS_YEEZY_350_V2`
  - ✅ `JORDAN-1-HIGH`
- **Ejemplos inválidos:**
  - ❌ `nike-air-jordan` (debe ser mayúsculas)
  - ❌ `AB` (muy corto, mínimo 3 caracteres)
  - ❌ `NIKE AIR JORDAN` (espacios no permitidos)

### Género
- Valores permitidos: `"MALE"`, `"FEMALE"`, `"UNISEX"`
- Case-sensitive: debe estar en mayúsculas

---

## 🚨 Manejo de Errores

### Errores Comunes

1. **SKU no encontrado (404)**
   - **Cuándo:** Se intenta obtener un SKU que no existe
   - **Acción:** Crear el sneaker primero usando `POST /api/catalog/sneakers`

2. **SKU duplicado (409)**
   - **Cuándo:** Se intenta crear un SKU que ya existe
   - **Acción:** Usar el sneaker existente, no es necesario crear otro

3. **SKU inválido (400)**
   - **Cuándo:** El formato del SKU no es válido
   - **Acción:** Validar el formato antes de enviar (mínimo 3 caracteres, solo mayúsculas, números, guiones y guiones bajos)

4. **Error del servidor (500)**
   - **Cuándo:** Error interno en el servidor
   - **Acción:** Reintentar después de unos segundos o contactar al administrador

---

## 💡 Tips para el Frontend

1. **Normalizar SKU:** Convertir a mayúsculas automáticamente en el input
   ```typescript
   const normalizedSku = sku.toUpperCase().trim();
   ```

2. **Validar antes de enviar:**
   ```typescript
   function isValidSku(sku: string): boolean {
     return /^[A-Z0-9_-]{3,50}$/.test(sku);
   }
   ```

3. **Debounce en búsquedas:** Esperar al menos 300ms después de que el usuario deje de escribir
   ```typescript
   const debouncedSearch = useMemo(
     () => debounce((query: string) => searchSneakers(query), 300),
     []
   );
   ```

4. **Cache de resultados:** Guardar resultados de búsqueda para evitar requests repetidos

---

## 🔗 URLs de Ejemplo

- **Local:** `http://localhost:8082/api/catalog/sneakers`
- **Producción:** `https://catalog-api.goat.com/api/catalog/sneakers`

---

**Última actualización:** 2025-01-21


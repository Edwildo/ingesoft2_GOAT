# Plan de Acción para Mejora del Repositorio GOAT

**Fecha:** 2025-11-21  
**Basado en:** Informe de Comparación de Ramas

---

## Priorización

### 🔴 CRÍTICO (Acción inmediata - Esta semana)

#### Paso 1: Remover credenciales del repositorio ⚡ [EN PROGRESO]
- **Objetivo:** Eliminar archivo `.env` del historial de Git
- **Impacto:** Seguridad crítica - credenciales expuestas
- **Esfuerzo:** 30 minutos
- **Pasos:**
  1. Remover `.env` del staging actual
  2. Agregar `.env` al `.gitignore` (ya está)
  3. Crear `.env.example` con estructura sin valores reales
  4. Rotar todas las credenciales expuestas
  5. Documentar proceso de configuración

#### Paso 2: Rotar credenciales comprometidas
- **Objetivo:** Cambiar todas las credenciales que estuvieron en el repositorio
- **Impacto:** Seguridad crítica
- **Esfuerzo:** 1-2 horas
- **Pasos:**
  1. Identificar todas las credenciales expuestas (MongoDB, SMTP)
  2. Generar nuevas credenciales
  3. Actualizar en MongoDB Atlas
  4. Actualizar configuración SMTP
  5. Actualizar `.env` local (no commitear)

### 🟡 ALTA PRIORIDAD (Próximas 2 semanas)

#### Paso 3: Implementar rate limiting
- **Objetivo:** Prevenir abuso de generación de OTPs
- **Impacto:** Seguridad y rendimiento
- **Esfuerzo:** 4-6 horas
- **Pasos:**
  1. Instalar `slowapi` o `fastapi-limiter`
  2. Implementar rate limiting por IP en endpoints OTP
  3. Implementar rate limiting por email
  4. Agregar headers de rate limit en respuestas
  5. Configurar límites apropiados (5 OTPs/min por email, 10/min por IP)
  6. Agregar tests para rate limiting

#### Paso 4: Sanitizar logs
- **Objetivo:** Eliminar información sensible de logs
- **Impacto:** Seguridad y privacidad
- **Esfuerzo:** 2-3 horas
- **Pasos:**
  1. Revisar todos los logs en código Python
  2. Remover OTPs completos de logs
  3. Remover hashes (incluso parciales) de logs de producción
  4. Usar nivel DEBUG solo en desarrollo
  5. Implementar función helper para sanitizar datos sensibles
  6. Actualizar documentación de logging

#### Paso 5: Refactorizar método `_create_email_body()`
- **Objetivo:** Mejorar mantenibilidad y separación de responsabilidades
- **Impacto:** Calidad de código
- **Esfuerzo:** 1-2 horas
- **Pasos:**
  1. Extraer creación de HTML a método `_create_html_body()`
  2. Considerar usar template engine (Jinja2) para emails
  3. Mover templates HTML a archivos separados
  4. Agregar tests para generación de emails

### 🟢 MEDIA PRIORIDAD (Próximo mes)

#### Paso 6: Automatizar inicialización de índices MongoDB
- **Objetivo:** Garantizar índices en producción automáticamente
- **Impacto:** Operacional
- **Esfuerzo:** 2-3 horas
- **Pasos:**
  1. Crear función helper `create_index_safe()` para reducir duplicación
  2. Ejecutar script en proceso de deployment
  3. Agregar health check que verifique índices
  4. Documentar proceso de deployment

#### Paso 7: Mejorar manejo de errores SMTP
- **Objetivo:** No exponer detalles internos y mejorar resiliencia
- **Impacto:** Seguridad y confiabilidad
- **Esfuerzo:** 3-4 horas
- **Pasos:**
  1. Crear excepciones de dominio para errores SMTP
  2. Implementar retry logic con exponential backoff
  3. Sanitizar mensajes de error para clientes
  4. Agregar logging estructurado de errores
  5. Considerar fallback a servicio alternativo

#### Paso 8: Validación SSL/TLS para SMTP
- **Objetivo:** Prevenir ataques MITM
- **Impacto:** Seguridad
- **Esfuerzo:** 1 hora
- **Pasos:**
  1. Configurar validación de certificados en producción
  2. Agregar configuración para desarrollo (skip validation)
  3. Documentar configuración

#### Paso 9: Extraer lógica duplicada de índices
- **Objetivo:** Reducir duplicación de código
- **Impacto:** Mantenibilidad
- **Esfuerzo:** 1 hora
- **Pasos:**
  1. Crear función `create_index_safe()` en script de inicialización
  2. Refactorizar código duplicado
  3. Agregar tests

#### Paso 10: Documentar configuración de email
- **Objetivo:** Facilitar configuración para nuevos desarrolladores
- **Impacto:** Documentación
- **Esfuerzo:** 1-2 horas
- **Pasos:**
  1. Agregar sección en README sobre configuración SMTP
  2. Documentar variables de entorno necesarias
  3. Incluir ejemplos para diferentes proveedores (Gmail, SendGrid, etc.)
  4. Agregar troubleshooting común

---

## Cronograma sugerido

### Semana 1
- ✅ Paso 1: Remover `.env` del repositorio
- ✅ Paso 2: Rotar credenciales
- 🔄 Paso 3: Implementar rate limiting (inicio)

### Semana 2
- ✅ Paso 3: Implementar rate limiting (completar)
- ✅ Paso 4: Sanitizar logs
- ✅ Paso 5: Refactorizar `_create_email_body()`

### Semana 3-4
- ✅ Paso 6: Automatizar índices MongoDB
- ✅ Paso 7: Mejorar manejo de errores SMTP
- ✅ Paso 8: Validación SSL/TLS

### Mes 2
- ✅ Paso 9: Extraer lógica duplicada
- ✅ Paso 10: Documentar configuración

---

## Métricas de éxito

- [ ] Archivo `.env` removido del historial Git
- [ ] Todas las credenciales rotadas
- [ ] Rate limiting implementado y funcionando
- [ ] 0 logs con información sensible en producción
- [ ] Código refactorizado sin duplicación crítica
- [ ] Índices MongoDB se crean automáticamente en deployment
- [ ] Documentación completa de configuración

---

## Notas

- Priorizar siempre seguridad sobre funcionalidad
- Cada paso debe incluir tests correspondientes
- Documentar cambios en commits descriptivos
- Revisar código en PR antes de merge


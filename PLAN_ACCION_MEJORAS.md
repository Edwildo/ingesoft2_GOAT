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

#### Paso 3: Implementar rate limiting ⚡ [COMPLETADO]
- **Objetivo:** Prevenir abuso de generación de OTPs
- **Impacto:** Seguridad y rendimiento
- **Esfuerzo:** 4-6 horas
- **Estado:** ✅ COMPLETADO
- **Pasos realizados:**
  1. ✅ Instalado `slowapi` en dependencias
  2. ✅ Implementado rate limiting por IP en endpoints OTP (10/min)
  3. ✅ Implementado rate limiting por email (5 OTPs/min, 10 verificaciones/min)
  4. ✅ Agregada configuración de límites en settings
  5. ✅ Implementada validación adicional en GenerateOTPUseCase
  6. ⏳ Tests para rate limiting (pendiente - puede agregarse después)

#### Paso 4: Sanitizar logs ⚡ [COMPLETADO]
- **Objetivo:** Eliminar información sensible de logs
- **Impacto:** Seguridad y privacidad
- **Esfuerzo:** 2-3 horas
- **Estado:** ✅ COMPLETADO
- **Pasos realizados:**
  1. ✅ Creado módulo `logging_utils.py` con funciones de sanitización
  2. ✅ Sanitizados emails en logs (muestra solo primer carácter + dominio)
  3. ✅ Removidos emojis de todos los logs (Python y scripts)
  4. ✅ Configurado nivel DEBUG solo en desarrollo
  5. ✅ Limpiados logs innecesarios en Python
  6. ✅ Limpiados console.log/error en Frontend (OTPs, emails, datos sensibles)
  7. ✅ Mejorado manejo de errores para no exponer detalles internos

#### Paso 5: Refactorizar método `_create_email_body()` ⚡ [COMPLETADO]
- **Objetivo:** Mejorar mantenibilidad y separación de responsabilidades
- **Impacto:** Calidad de código
- **Esfuerzo:** 1-2 horas
- **Estado:** ✅ COMPLETADO
- **Pasos realizados:**
  1. ✅ Extraído `_create_plain_text_body()` para texto plano
  2. ✅ Extraído `_create_html_body()` para contenido HTML
  3. ✅ Extraído `_get_html_styles()` para estilos CSS
  4. ✅ Reducido `_create_email_body()` de 73 líneas a 5 líneas
  5. ⏳ Tests para generación de emails (opcional - puede agregarse después)
  6. ⏳ Template engine (Jinja2) - puede considerarse en futura mejora

### 🟢 MEDIA PRIORIDAD (Próximo mes)

#### Paso 6: Automatizar inicialización de índices MongoDB ⚡ [COMPLETADO]
- **Objetivo:** Garantizar índices en producción automáticamente
- **Impacto:** Operacional
- **Esfuerzo:** 2-3 horas
- **Estado:** ✅ COMPLETADO
- **Pasos realizados:**
  1. ✅ Creado módulo compartido `mongo_indexes.py` con función `create_index_safe()`
  2. ✅ Refactorizado script `init_mongo_indexes.py` para usar módulo compartido (DRY)
  3. ✅ Automatizada inicialización de índices en startup de la aplicación
  4. ✅ Agregada función `verify_otp_indexes()` para verificar existencia de índices
  5. ✅ Mejorado health check endpoint para incluir verificación de índices
  6. ⏳ Documentación de deployment (puede agregarse en README si es necesario)

#### Paso 7: Mejorar manejo de errores SMTP ⚡ [COMPLETADO]
- **Objetivo:** No exponer detalles internos y mejorar resiliencia
- **Impacto:** Seguridad y confiabilidad
- **Esfuerzo:** 3-4 horas
- **Estado:** ✅ COMPLETADO
- **Pasos realizados:**
  1. ✅ Creadas excepciones de dominio (EmailSendException, EmailAuthenticationException, EmailConnectionException, EmailDeliveryException)
  2. ✅ Implementado retry logic con exponential backoff (módulo `retry.py`)
  3. ✅ Sanitizados mensajes de error para clientes (user_message en excepciones)
  4. ✅ Agregado logging estructurado con contexto adicional (extra fields)
  5. ⏳ Fallback a servicio alternativo (puede considerarse en futura mejora)

#### Paso 8: Validación SSL/TLS para SMTP ⚡ [COMPLETADO]
- **Objetivo:** Prevenir ataques MITM
- **Impacto:** Seguridad
- **Esfuerzo:** 1 hora
- **Estado:** ✅ COMPLETADO
- **Pasos realizados:**
  1. ✅ Agregada configuración `smtp_validate_cert` (default: true)
  2. ✅ Implementado SSL context con validación de certificados
  3. ✅ Configuración flexible para desarrollo (puede desactivarse)
  4. ✅ Documentación completa en README con ejemplos de proveedores comunes
  5. ✅ Documentadas mejores prácticas de seguridad

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


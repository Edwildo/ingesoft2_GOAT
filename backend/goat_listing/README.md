# GOAT Listing Backend

Backend API Spring Boot para gestionar autenticación (JWT/OTP), catálogo de sneakers y publicaciones (listings), siguiendo una arquitectura hexagonal con módulos: identity, listing, navigation y cart.

## Sistema propuesto

- API REST stateless
- Autenticación y autorización vía JWT
- Flujo de OTP (2FA-like) para registro, login y confirmación de email
- Gestión de listings (crear, actualizar, publicar, archivar) y consulta de catálogo
- Reglas de acceso por rol (ADMIN, SELLER, BUYER)

## Alcance en seguridad

La API incorporará varias medidas de seguridad (RNF):

### Implementaciones

- Autenticación JWT y establecimiento del contexto
  - Filtro: `JwtAuthenticationFilter` valida `Authorization: Bearer <token>` y llena el `SecurityContext` con `userId` y roles.
  ```java
  // JwtAuthenticationFilter#doFilterInternal
  String token = authHeader.substring("Bearer ".length());
  UUID userId = jwtTokenHelper.extractUserId(token);
  List<String> roles = jwtTokenHelper.extractRoles(token);
  var authorities = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
  var authentication = new UsernamePasswordAuthenticationToken(userId.toString(), null, authorities);
  SecurityContextHolder.getContext().setAuthentication(authentication);
  ```
  - Parsing seguro del token: `JwtTokenHelper` verifica la firma.
  ```java
  // JwtTokenHelper#parseToken
  return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  ```
- Sesiones stateless y CSRF deshabilitado para API JWT
  ```java
  http.csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
  ```
- Hashing de contraseñas (BCrypt)
  ```java
  // BCryptPasswordEncoderAdapter
  passwordEncoder.encode(rawPassword);
  passwordEncoder.matches(rawPassword, hash);
  ```
- Manejo centralizado de errores de seguridad/dominio (`ExceptionHandlerConfig`) con respuestas 401/403/409/400.
- Flujo OTP/2FA a nivel de casos de uso y endpoints (`/api/auth/otp`, `/api/auth/verify`).

- Cabeceras de seguridad (OWASP: Clickjacking, MIME sniffing, Referrer policy, HSTS)
  ```java
  // SecurityConfig.headers(...)
  headers -> headers
      .contentTypeOptions(Customizer.withDefaults()) // X-Content-Type-Options: nosniff
      .frameOptions(f -> f.deny())                   // X-Frame-Options: DENY (Clickjacking)
      .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER));
  ```
- Autorización por rol en endpoints sensibles
  ```java
  // SecurityConfig.authorizeHttpRequests(...)
  .requestMatchers(HttpMethod.POST, "/api/listings").hasRole("SELLER")
  .requestMatchers(HttpMethod.POST, "/api/catalog/sneakers").hasRole("ADMIN")
  .anyRequest().authenticated();
  ```
- Fortalecimiento del secreto JWT (fail-fast si es débil o por defecto)
  ```java
  // JwtTokenHelper constructor
  if (!StringUtils.hasText(secret) || DEFAULT_SECRET.equals(secret) || secret.getBytes(UTF_8).length < 32) {
      throw new IllegalStateException("JWT secret inválido: define jwt.secret (>=32 bytes)");
  }
  ```
- Rate limiting básico en login/OTP (protección anti brute force)
  ```java
  // AuthController: Sliding window por IP+email
  if (isBlocked(key)) { return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build(); }
  try { loginUserUseCase.execute(request); registerSuccess(key); } catch (Exception ex) { registerFailure(key); throw ex; }
  ```
- Validación de DTOs (entrada acotada)
  ```java
  // LoginRequest
  @Email @NotBlank String email; @NotBlank @Size(min = 8) String password;
  // VerifyOtpRequest
  @Pattern(regexp = "^[0-9]{4,8}$") String otp;
  // Create/UpdateListingRequest (ejemplos de límites)
  @Size(max = 64) String sneakerSku; @Size(max = 16) String size; @Size(max = 32) String condition;
  ```

### Uso de JPA y relación con OWASP

La capa de persistencia usa Spring Data JPA y entidades mapeadas para acceder a la base de datos. Esto ayuda a mitigar inyecciones (OWASP A03: Injection) al usar consultas parametrizadas y evitar concatenación de SQL manual.

Ejemplos:

- Repositorios JPA (consultas parametrizadas por defecto)
```java
// ListingJpaRepository
public interface ListingJpaRepository extends JpaRepository<ListingEntity, UUID> {
    Optional<ListingEntity> findById(UUID id);
    List<ListingEntity> findByOwnerId(UUID ownerId);
}
```

- Entidades y mapeos controlados
```java
// ListingEntity
@Entity
@Table(name = "listings")
public class ListingEntity {
    @Id
    private UUID id;
    private UUID ownerId;
    private String sneakerSku;
    // ... campos mapeados de forma tipada
}
```

- Adaptadores de persistencia: no se construyen SQLs concatenando strings; se delega a JPA
```java
// PostgreSQLListingRepository
public class PostgreSQLListingRepository implements ListingRepository {
    private final ListingJpaRepository jpa;
    @Override
    public Optional<Listing> findById(UUID id) {
        return jpa.findById(id).map(ListingMapper::toDomain);
    }
}
```

Buenas prácticas aplicadas:
- Consultas derivadas o `@Query` con parámetros → evita injection.
- Tipos fuertes en entidades → reduce riesgo de cast y desbordes.
- Validación previa en DTOs/controladores → inputs maliciosos se rechazan antes de llegar a la BD.
- Sin uso de SQL dinámico concatenado en controladores/adaptadores.

### Relación con OWASP Top 10
- A01: Broken Access Control → reglas por rol y ownership en casos de uso.
- A02: Cryptographic Failures → JWT firmado con clave fuerte, BCrypt para contraseñas.
- A03: Injection → Validación DTOs, uso de JPA con parámetros, evitar concatenaciones.
- A05: Security Misconfiguration → Headers seguros, CORS configurado, CSRF deshabilitado correctamente para API JWT.
- A07: Identification and Authentication Failures → JWT, OTP, rate limiting en login.
- A10: Server-Side Request Forgery (SSRF) → revisar `RestTemplateConfig` (base URL segura, builder URIs).

## Casos de prueba

1) Verificar cabeceras de seguridad (navegador o curl)
- Lanzar la API y accede a cualquier endpoint público (ej. `GET /api/listings`).
- Observar en las devtools del navegador (Network → Headers):

  Debe incluir:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `Referrer-Policy: no-referrer`

2) Verificar autorización por rol
- Prueba `POST /api/listings` con un JWT que tenga `roles: ["SELLER"]` → 201/200.
- Prueba con JWT que no tenga `SELLER` → 403.

3) Verificar validaciones de DTOs
- `POST /api/auth/register` con `password` corta → 400 con errores.
- `POST /api/auth/verify` con `otp` no numérico → 400.

4) Verificar rate limiting en login/OTP
- Realizar 5 intentos fallidos seguidos a `/api/auth/login` desde la misma IP/email.
- El 6º intento dentro de 5 minutos debe devolver `429 Too Many Requests`.

5) Verificar JWT en llamadas autenticadas
- Realiza `GET` a un endpoint autenticado sin `Authorization` → 401.
- Con `Authorization: Bearer <token>` válido → 200 y acceso.

## Pantallazos de pruebas

- [ ] Headers de seguridad en `GET /api/listings`
- [ ] 403 en `POST /api/catalog/sneakers` sin rol ADMIN
- [ ] 429 en `/api/auth/login` tras intentos fallidos
- [ ] 400 por validación de DTOs (password corta, otp inválido)
- [ ] 401 sin JWT y 200 con JWT válido


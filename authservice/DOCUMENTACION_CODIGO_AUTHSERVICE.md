# Documentacion Tecnica de Codigo - authservice

## 1. Objetivo del modulo

`authservice` implementa autenticacion basada en JWT para registro, login, refresh de tokens y consulta de perfil del usuario autenticado. Incluye:

- Persistencia de usuarios y refresh tokens.
- Seguridad stateless con Spring Security.
- Trazabilidad por correlation id.
- Integracion externa resiliente con Circuit Breaker.
- API documentada con OpenAPI/Swagger.

## 2. Flujo funcional principal

1. Registro (`POST /api/auth/register`): crea usuario, cifra password, genera access token + refresh token.
2. Login (`POST /api/auth/login`): autentica credenciales, invalida refresh anteriores, emite nuevo par de tokens.
3. Refresh (`POST /api/auth/refresh`): rota refresh token (revoca el anterior y crea uno nuevo).
4. Perfil (`GET /api/users/me`): retorna datos basicos del usuario autenticado.
5. Validacion (`GET /api/auth/validate`): valida access token para uso de gateway.

## 3. Estructura por paquete y responsabilidades

### 3.1 Arranque

- `src/main/java/com/main/authservice/AuthserviceApplication.java`
  - Punto de entrada Spring Boot (`main`).

### 3.2 Controllers

- `src/main/java/com/main/authservice/controller/AuthController.java`
  - Endpoints de autenticacion:
    - `register(RegisterRequest)`
    - `login(LoginRequest)`
    - `refresh(RefreshRequest)`
    - `validateAccessToken(String authorization)`

- `src/main/java/com/main/authservice/controller/UserController.java`
  - Endpoint de usuario autenticado:
    - `me(Authentication)`

### 3.3 Services

- `src/main/java/com/main/authservice/service/AuthService.java`
  - Logica de negocio de autenticacion:
    - `register(RegisterRequest)`
    - `login(LoginRequest)`
    - `refresh(RefreshRequest)`
    - `me(String email)`
  - Helpers internos:
    - `buildAuthResponse(User, String)`
    - `createAndSaveRefreshToken(User)`
    - `generateRefreshTokenValue()`

- `src/main/java/com/main/authservice/service/ExternalIntegrationService.java`
  - Integracion externa posterior a registro con Circuit Breaker.
  - Metodos:
    - `notifyUserRegistered(User, String)`
    - `getCircuitBreakerStatus()`

### 3.4 Security

- `src/main/java/com/main/authservice/security/JwtService.java`
  - Generacion y validacion de JWT.
  - Metodos publicos:
    - `generateAccessToken(UserDetails)`
    - `generateAccessToken(String)`
    - `extractSubject(String)`
    - `isTokenValid(String, UserDetails)`
    - `isTokenValid(String)`

- `src/main/java/com/main/authservice/security/JwtAuthenticationFilter.java`
  - Filtro `OncePerRequestFilter` que autentica request con Bearer token.

- `src/main/java/com/main/authservice/security/CustomUserDetailsService.java`
  - Adaptador entre `UserRepository` y `UserDetailsService`.

### 3.5 Configuracion

- `src/main/java/com/main/authservice/config/SecurityConfig.java`
  - Configura `SecurityFilterChain`, CORS, `PasswordEncoder`, `AuthenticationManager`.

- `src/main/java/com/main/authservice/config/OpenApiConfig.java`
  - Define metadata OpenAPI de authservice.

- `src/main/java/com/main/authservice/config/ExternalIntegrationsProperties.java`
  - Propiedades type-safe (`@ConfigurationProperties`) para integraciones externas.

- `src/main/java/com/main/authservice/config/CorrelationIdFilter.java`
  - Resuelve/genera `X-Correlation-ID` y lo registra en MDC para logs.

### 3.6 Persistencia

- `src/main/java/com/main/authservice/repository/UserRepository.java`
  - Acceso a datos de usuarios.

- `src/main/java/com/main/authservice/repository/RefreshTokenRepository.java`
  - Acceso a datos de refresh tokens y consultas por token/estado.

### 3.7 Modelo de dominio

- `src/main/java/com/main/authservice/model/User.java`
  - Entidad de usuario.
- `src/main/java/com/main/authservice/model/RefreshToken.java`
  - Entidad de refresh token.
- `src/main/java/com/main/authservice/model/Role.java`
  - Enum de roles de usuario.

### 3.8 DTOs

- `src/main/java/com/main/authservice/dto/RegisterRequest.java`
- `src/main/java/com/main/authservice/dto/LoginRequest.java`
- `src/main/java/com/main/authservice/dto/RefreshRequest.java`
- `src/main/java/com/main/authservice/dto/AuthResponse.java`
- `src/main/java/com/main/authservice/dto/UserMeResponse.java`

Uso: contrato de entrada/salida HTTP y validaciones.

### 3.9 Manejo de errores

- `src/main/java/com/main/authservice/exception/ApiExceptionHandler.java`
  - Mapeo global de excepciones a `ProblemDetail`.
- `src/main/java/com/main/authservice/exception/ConflictException.java`
- `src/main/java/com/main/authservice/exception/UnauthorizedException.java`

### 3.10 Integraciones externas (Factory Method)

- `src/main/java/com/main/authservice/external/ExternalConnector.java`
  - Contrato de conector externo.
- `src/main/java/com/main/authservice/external/ErpConnector.java`
- `src/main/java/com/main/authservice/external/PosConnector.java`
- `src/main/java/com/main/authservice/external/ConnectorFactory.java`
  - Fabrica de conectores por tipo.
- `src/main/java/com/main/authservice/external/ExternalIntegrationExample.java`
  - Ejemplo de uso.

### 3.11 Actuator

- `src/main/java/com/main/authservice/actuator/ExternalCircuitBreakerEndpoint.java`
  - Endpoint para exponer estado del circuit breaker de integraciones externas.

## 4. Pruebas (src/test/java)

- `src/test/java/com/main/authservice/AuthserviceApplicationTests.java`
  - Carga de contexto Spring.
- `src/test/java/com/main/authservice/controller/AuthControllerTest.java`
  - Pruebas del controlador de autenticacion.
- `src/test/java/com/main/authservice/service/AuthServiceTest.java`
  - Casos funcionales de servicio principal.
- `src/test/java/com/main/authservice/service/AuthServiceSecurityTest.java`
  - Casos de seguridad y reglas asociadas.
- `src/test/java/com/main/authservice/service/AuthServicePasswordHashingTest.java`
  - Verificacion de hashing/encoder de passwords.
- `src/test/java/com/main/authservice/service/ExternalIntegrationFactoryMethodTest.java`
  - Validacion del patron Factory Method.
- `src/test/java/com/main/authservice/integration/AuthServiceIntegrationTest.java`
  - Integracion de componentes internos.
- `src/test/java/com/main/authservice/integration/AuthServiceE2ETest.java`
  - Escenarios end-to-end.
- `src/test/java/com/main/authservice/integration/AuthServicePerformanceTest.java`
  - Pruebas de rendimiento basicas.

## 5. Buenas practicas aplicadas

- Inyeccion por constructor en servicios/controladores.
- Servicios de negocio transaccionales (`@Transactional`).
- DTOs para no exponer entidades directamente en API.
- Manejo centralizado de errores con `@RestControllerAdvice`.
- Seguridad stateless con JWT y filtro dedicado.
- Trazabilidad con correlation id en MDC.
- Resiliencia en integraciones externas con Circuit Breaker.

## 6. Recomendacion de documentacion incremental

Para continuar con "comentar todo el proyecto" sin ensuciar el codigo:

1. Mantener JavaDoc en clases/metodos publicos clave.
2. Evitar comentarios redundantes en getters/setters triviales.
3. Documentar decisiones de arquitectura en archivos markdown (como este).
4. Agregar diagramas de secuencia para login/refresh en `docs/`.

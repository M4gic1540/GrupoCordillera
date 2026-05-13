# API Gateway

Puerta de entrada unica para el ecosistema backend. Centraliza rutas, CORS y autenticacion para endpoints protegidos.

## Stack

- Java 17
- Spring Boot 3.3.x
- Spring Cloud Gateway

## Endpoints principales

- `GET /actuator/health`: salud del gateway.
- Rutas proxied bajo `/bff/**` hacia `bff-service`.

## Reglas de seguridad

- Rutas protegidas: `/bff/**`
- Rutas excluidas (publicas):
  - `/bff/auth/login`
  - `/bff/auth/register`
  - `/bff/auth/refresh`
  - `/bff/system/health`
  - rutas de documentacion OpenAPI

## Variables de entorno relevantes

- `AUTH_VALIDATION_URL`
- `BFF_SERVICE_URL`
- `FRONTEND_ORIGIN`

## Ejecucion local (Maven)

```bash
./mvnw spring-boot:run
```

## Pruebas

```bash
./mvnw test
```

## Observaciones

- El filtro global `AuthenticationFilter` valida JWT delegando en Auth Service.
- Se recomienda consumir siempre por el gateway desde el frontend.

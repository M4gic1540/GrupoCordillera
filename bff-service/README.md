# BFF Service (Backend For Frontend)

Este servicio actúa como una capa de agregación y proxy para el frontend. Su objetivo es consolidar llamadas a múltiples microservicios y adaptar las respuestas a las necesidades de la interfaz de usuario.

## Características
- **Agregación**: Endpoint `/bff/dashboard` que combina salud de servicios y KPIs.
- **Proxy**: Redirige peticiones a `auth-service` y `kpi-engine`.
- **Resiliencia**: Implementa timeouts y fallbacks básicos para servicios downstream.
- **DTOs propios**: No expone entidades internas, usa objetos de transferencia de datos específicos.

## Tecnologías
- Java 17
- Spring Boot 3.3.5
- Spring WebFlux (Reactivo)
- SpringDoc OpenAPI

## Endpoints Principales (Directos puerto 8084)
*Aunque se recomienda acceder vía Gateway (8080).*

- `GET /bff/dashboard`: Dashboard consolidado.
- `GET /bff/system/health`: Salud de servicios dependientes.
- `GET /bff/docs`: Swagger UI.

## Configuración
Las URLs de los servicios dependientes se configuran vía variables de entorno:
- `AUTHSERVICE_URL`: Base URL de Auth Service.
- `INGESTION_URL`: Base URL de Data Ingestion.
- `KPI_ENGINE_URL`: Base URL de KPI Engine.


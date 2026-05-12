# KPI Engine

Microservicio encargado de calcular y exponer indicadores KPI a partir de eventos de ingesta.

## Stack

- Java 17
- Spring Boot 3.3.x
- Spring Data JPA
- PostgreSQL

## Endpoints principales

- `POST /api/kpi/recalculate`: recalcula indicadores segun fuente y registros afectados.
- `GET /api/kpi/snapshots/latest`: retorna snapshots mas recientes.
- `GET /api/kpi/health`: estado de salud del servicio.

## Variables de entorno relevantes

- `KPI_DB_URL`
- `KPI_DB_USERNAME`
- `KPI_DB_PASSWORD`

## Ejecucion local (Maven)

```bash
./mvnw spring-boot:run
```

## Pruebas

```bash
./mvnw test
```

## Observaciones

- Aplica `Repository Pattern` para definiciones y snapshots.
- Expone DTOs desacoplados de entidades para el consumo del BFF.


# Data Ingestion Service

Microservicio de ingesta de datos externos. Su objetivo es consumir eventos de sistemas fuente, validarlos, persistirlos y disparar recalculo de KPIs.

## Stack

- Java 17
- Spring Boot 3.3.x
- Spring Data JPA
- PostgreSQL
- Resilience4j

## Endpoints principales

- `POST /api/ingestion/sync/{sourceSystem}`: ejecuta ingesta para una fuente (`crm`, `erp`, `hr`, etc.).
- `GET /api/ingestion/health`: estado de salud del servicio.

## Variables de entorno relevantes

- `EVENTS_DB_URL`
- `EVENTS_DB_USERNAME`
- `EVENTS_DB_PASSWORD`
- `KPI_ENGINE_BASE_URL`

## Ejecucion local (Maven)

```bash
./mvnw spring-boot:run
```

## Ejecucion con Docker Compose

Desde `backendjava`:

```bash
docker-compose up --build
```

## Pruebas

```bash
./mvnw test
```

## Observaciones

- El servicio usa `ConnectorFactory` para resolver conectores por fuente.
- Implementa validaciones de payload y deduplicacion en memoria por corrida.

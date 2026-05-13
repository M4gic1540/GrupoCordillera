# Proyecto Semestral - Monitoreo de KPIs (Backend Java)

Este proyecto es una arquitectura de microservicios diseñada para la ingesta de datos, procesamiento de KPIs y exposición de resultados a través de un BFF (Backend For Frontend).

## Arquitectura
La comunicación externa está centralizada a través de un **API Gateway**, que redirige todas las peticiones al **BFF**. Los microservicios internos no deben ser accedidos directamente por el frontend.

### Componentes:
- **API Gateway (Puerto 8080)**: Punto de entrada único, maneja la seguridad y el ruteo.
- **BFF Service (Puerto 8084)**: Agregador de datos y proxy para el frontend.
- **Auth Service (Interno 8080)**: Gestión de usuarios y tokens JWT.
- **KPI Engine (Puerto 8082)**: Procesamiento y almacenamiento de indicadores.
- **Data Ingestion (Puerto 8081)**: Recepción y normalización de datos de fuentes externas.

---

## Catálogo de Endpoints Actualizados

Todas las rutas principales deben prefijarse con el host del Gateway: `http://localhost:8080`

### 🔑 Autenticación (Públicos)
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/bff/auth/login` | Autentica un usuario y devuelve JWT (Access & Refresh). |
| `POST` | `/bff/auth/register` | Registra un nuevo usuario en el sistema. |
| `POST` | `/bff/auth/refresh` | Renueva el Access Token usando un Refresh Token. |

### 👤 Usuario (Protegido)
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/bff/auth/me` | Obtiene los detalles del usuario autenticado. |

### 📊 Dashboard e Indicadores (Protegidos)
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/bff/dashboard` | Retorna salud de servicios y últimos KPIs en una sola llamada. |
| `GET` | `/bff/kpis/latest` | Obtiene la lista de los últimos snapshots de KPIs. |
| `POST` | `/bff/kpis/recalculate` | Solicita el recálculo manual de KPIs. |

### 🛠️ Sistema y Monitoreo (Público)
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/bff/system/health` | Estado de salud detallado de todos los microservicios dependientes. |

---

## Pruebas e Ingestión de Datos
Dado que el acceso directo a los microservicios está restringido en el Gateway (excepto `/bff/**`), para simular la entrada de datos desde sistemas externos se utiliza directamente el puerto expuesto del servicio de ingesta:

- **Ingesta Manual**: `POST http://localhost:8081/api/ingestion/sync/{sourceSystem}`
  - Ejemplos de `{sourceSystem}`: `crm`, `erp`, `hr`.
  - Este endpoint dispara internamente el recálculo en el **KPI Engine**.

---

## Seguridad
- La mayoría de los endpoints bajo `/bff/**` requieren un encabezado de autorización:
  `Authorization: Bearer <TU_TOKEN_JWT>`
- Los tokens se obtienen a través de los endpoints de `/bff/auth/`.

## Documentación (Swagger)
El proyecto utiliza SpringDoc para generar documentación OpenAPI. Puedes acceder a ella a través del Gateway:
- **Swagger UI**: [http://localhost:8080/bff/docs](http://localhost:8080/bff/docs)
- **JSON Spec**: [http://localhost:8080/bff/v3/api-docs](http://localhost:8080/bff/v3/api-docs)

## Ejecución con Docker
Para levantar todo el ecosistema:
```bash
docker-compose up --build
```

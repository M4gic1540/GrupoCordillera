# AuthService

Microservicio de autenticación desarrollado en Java Spring Boot. Proporciona endpoints para registro y autenticación de usuarios, gestionando credenciales y roles de manera segura.

## Características principales
- Registro y autenticación de usuarios
- Integración con base de datos PostgreSQL
- Seguridad con Spring Security
- Contenedorización con Docker y docker-compose
- Soporte para múltiples entornos (main, developer, feature)

## Estructura del proyecto
- `src/main/` - Código fuente principal
- `src/test/` - Pruebas unitarias
- `docs/` - Documentación (excluida del repositorio)
- `docker-compose.yml` y `Dockerfile` - Configuración de contenedores

## Uso rápido
1. Clona el repositorio
2. Configura las variables de entorno necesarias
3. Ejecuta `docker-compose up --build`
4. Accede a los endpoints en `/api/auth`

## Endpoints principales
- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Autenticación de usuario

## Requisitos
- Java 25
- Maven
- Docker
- PostgreSQL 15

## Contribución
Utiliza ramas `developer` y `feature` para desarrollo y nuevas funcionalidades. Los cambios a `main` requieren revisión y pruebas.

---

> Para más detalles, consulta la documentación interna o contacta al equipo de desarrollo.

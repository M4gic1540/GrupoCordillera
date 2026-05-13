# Arquetipos Maven

Este modulo contiene arquetipos Maven para generar nuevos servicios backend con estructura estandar.

## Arquetipo incluido

- `microservice-archetype`: plantilla base Spring Boot para microservicios REST.

## Requisitos

- Maven 3.9+
- Java 17+

## Instalar arquetipo en repositorio local

Desde `backendjava/arquetipos-maven/microservice-archetype`:

```bash
mvn clean install
```

## Generar proyecto desde arquetipo

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.main.archetypes \
  -DarchetypeArtifactId=microservice-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.main \
  -DartifactId=nuevo-servicio \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackage=com.main.nuevoservicio \
  -DinteractiveMode=false
```

## Salida esperada

El comando anterior genera:

- `pom.xml` con dependencias base Spring Boot
- `src/main/java/.../Application.java`
- `src/main/java/.../controller/HealthController.java`
- `src/main/resources/application.yml`
- `src/test/java/.../ApplicationTests.java`

## Recomendacion de uso

Utilizar este arquetipo como punto de partida para nuevos microservicios y aplicar convenciones de rama/documentacion definidas en la entrega.

FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 1. copiar solo pom (cache de dependencias)
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# 2. copiar código
COPY src ./src

# 3. compilar
RUN mvn -q -DskipTests clean package

# runtime liviano
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN addgroup --system app && adduser --system --ingroup app app
USER app

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
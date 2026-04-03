FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

COPY pom.xml ./
COPY .mvn ./.mvn
COPY mvnw ./
RUN chmod +x mvnw

# Resolve dependencies first to improve Docker layer caching.
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src ./src

RUN ./mvnw -DskipTests clean package

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

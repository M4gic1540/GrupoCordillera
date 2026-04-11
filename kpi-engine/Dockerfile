FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN addgroup --system app && adduser --system --ingroup app app
USER app

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
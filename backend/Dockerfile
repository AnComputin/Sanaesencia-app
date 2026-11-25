# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-jammy AS builder
ARG SERVICE_DIR
WORKDIR /app

COPY ${SERVICE_DIR}/.mvn .mvn
COPY ${SERVICE_DIR}/mvnw ${SERVICE_DIR}/pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY ${SERVICE_DIR}/src src
RUN ./mvnw -B clean package -DskipTests && mv target/*.jar app.jar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/app.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

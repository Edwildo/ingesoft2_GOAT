# Multi-stage Dockerfile: build the jar with Maven then create a slim runtime image
FROM maven:3.8.8-openjdk-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src

# Avoid downloading unnecessary plugins during the build and reuse cache
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
ARG JAR_FILE=target/goat-listing-1.0.0.jar
COPY --from=build /workspace/${JAR_FILE} /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
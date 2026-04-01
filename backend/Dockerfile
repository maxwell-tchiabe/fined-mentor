# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

ARG SENTRY_AUTH_TOKEN

RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user and group for security
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy the jar file and assign ownership to the non-root user
COPY --from=build --chown=spring:spring /app/target/mentor-0.0.1-SNAPSHOT.jar app.jar

# Switch to the non-root user
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# Build stage
FROM maven:3.8.4-openjdk-17-slim AS builder

# Set working directory
WORKDIR /app

# Copy Maven settings first
COPY settings.xml /root/.m2/settings.xml

# Copy Maven files first for better caching
COPY pom.xml .
COPY trade-data-app/pom.xml trade-data-app/
COPY trade-data-service/pom.xml trade-data-service/
COPY trade-data-kafka/pom.xml trade-data-kafka/
COPY trade-data-common/pom.xml trade-data-common/
COPY trade-data-api/pom.xml trade-data-api/
COPY trade-data-processor/pom.xml trade-data-processor/
COPY trade-data-scheduler/pom.xml trade-data-scheduler/
COPY trade-data-external-api/pom.xml trade-data-external-api/

# Copy source code
COPY trade-data-app/src trade-data-app/src/
COPY trade-data-service/src trade-data-service/src/
COPY trade-data-kafka/src trade-data-kafka/src/
COPY trade-data-common/src trade-data-common/src/
COPY trade-data-api/src trade-data-api/src/
COPY trade-data-processor/src trade-data-processor/src/
COPY trade-data-scheduler/src trade-data-scheduler/src/
COPY trade-data-external-api/src trade-data-external-api/src/

# Build the application with GitHub credentials
ARG GITHUB_PACKAGES_USERNAME
# Use build secret for token instead of ARG
RUN --mount=type=secret,id=github_token \
    export GITHUB_PACKAGES_TOKEN="$(cat /run/secrets/github_token)" && \
    mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built artifact from builder stage
COPY --from=builder /app/trade-data-app/target/*.jar app.jar

# Install curl for healthcheck
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    # Set timezone
    ln -sf /usr/share/zoneinfo/Asia/Kolkata /etc/localtime

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=Asia/Kolkata

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

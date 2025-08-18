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
COPY trade-data-model/pom.xml trade-data-model/

# Create source directories to avoid COPY failures
RUN mkdir -p trade-data-app/src \
    trade-data-service/src \
    trade-data-kafka/src \
    trade-data-common/src \
    trade-data-api/src \
    trade-data-processor/src \
    trade-data-scheduler/src \
    trade-data-external-api/src \
    trade-data-model/src

# Copy source code with error handling
COPY trade-data-app/src trade-data-app/src/ || true
COPY trade-data-service/src trade-data-service/src/ || true
COPY trade-data-kafka/src trade-data-kafka/src/ || true
COPY trade-data-common/src trade-data-common/src/ || true
COPY trade-data-api/src trade-data-api/src/ || true
COPY trade-data-processor/src trade-data-processor/src/ || true
COPY trade-data-scheduler/src trade-data-scheduler/src/ || true
COPY trade-data-external-api/src trade-data-external-api/src/ || true
COPY trade-data-model/src trade-data-model/src/ || true

# Build the application with GitHub credentials
ARG GITHUB_PACKAGES_USERNAME
ARG GITHUB_PACKAGES_TOKEN
ENV GITHUB_PACKAGES_USERNAME=${GITHUB_PACKAGES_USERNAME}
ENV GITHUB_PACKAGES_TOKEN=${GITHUB_PACKAGES_TOKEN}

# Build the application
RUN mvn clean package -DskipTests

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

# Health check with JSON format
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

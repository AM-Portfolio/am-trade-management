# Build stage
FROM maven:3.8.4-openjdk-17-slim AS builder

WORKDIR /build

# Copy Maven settings and pom files first for better caching
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY */pom.xml ./*/

# Download dependencies first (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY . .

# Build with Maven
ARG GITHUB_PACKAGES_USERNAME
ARG GITHUB_PACKAGES_TOKEN
RUN mvn clean package -DskipTests \
    -DGITHUB_PACKAGES_USERNAME=${GITHUB_PACKAGES_USERNAME} \
    -DGITHUB_PACKAGES_TOKEN=${GITHUB_PACKAGES_TOKEN}

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy only the built jar from builder stage
COPY --from=builder /build/trade-data-app/target/*.jar app.jar

# Install curl and set timezone
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    ln -sf /usr/share/zoneinfo/Asia/Kolkata /etc/localtime

# Environment setup
ENV SPRING_PROFILES_ACTIVE=docker \
    TZ=Asia/Kolkata \
    JAVA_OPTS="-Xms512m -Xmx1024m"

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

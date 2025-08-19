# Build stage
FROM --platform=${BUILDPLATFORM:-linux/amd64} maven:3.8.4-openjdk-17-slim AS builder

WORKDIR /build

# Copy Maven settings first
COPY settings.xml /root/.m2/settings.xml

# Copy root pom.xml
COPY pom.xml .

# Copy all module pom files
COPY am-trade-common/pom.xml am-trade-common/
COPY am-trade-models/pom.xml am-trade-models/
COPY am-trade-services/pom.xml am-trade-services/
COPY am-trade-dashboard/pom.xml am-trade-dashboard/
COPY am-trade-api/pom.xml am-trade-api/
COPY am-trade-app/pom.xml am-trade-app/
COPY am-trade-kafka/pom.xml am-trade-kafka/
COPY am-trade-persistence/pom.xml am-trade-persistence/
COPY am-trade-exceptions/pom.xml am-trade-exceptions/

# Download dependencies with debug output
RUN mvn -X dependency:go-offline -B || (echo "Maven dependency download failed" && exit 1)

# Copy all module source files
COPY am-trade-common/src am-trade-common/src/
COPY am-trade-models/src am-trade-models/src/
COPY am-trade-services/src am-trade-services/src/
COPY am-trade-dashboard/src am-trade-dashboard/src/
COPY am-trade-api/src am-trade-api/src/
COPY am-trade-app/src am-trade-app/src/
COPY am-trade-kafka/src am-trade-kafka/src/
COPY am-trade-persistence/src am-trade-persistence/src/
COPY am-trade-exceptions/src am-trade-exceptions/src/

# Build with Maven
ARG GITHUB_PACKAGES_USERNAME
ARG GITHUB_PACKAGES_TOKEN
RUN mvn clean package -DskipTests -X \
    -DGITHUB_PACKAGES_USERNAME=${GITHUB_PACKAGES_USERNAME} \
    -DGITHUB_PACKAGES_TOKEN=${GITHUB_PACKAGES_TOKEN}

# Runtime stage
FROM --platform=${TARGETPLATFORM:-linux/amd64} eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy only the built jar from builder stage with correct path
COPY --from=builder /build/am-trade-app/target/*.jar app.jar

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

# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy the pom.xml and all source modules
COPY pom.xml .
COPY am-trade-common am-trade-common
COPY am-trade-models am-trade-models
COPY am-trade-services am-trade-services
COPY am-trade-dashboard am-trade-dashboard
COPY am-trade-api am-trade-api
COPY am-trade-app am-trade-app
COPY am-trade-kafka am-trade-kafka
COPY am-trade-persistence am-trade-persistence
COPY am-trade-exceptions am-trade-exceptions
COPY am-trade-analytics am-trade-analytics
COPY am-trade-sdk-java am-trade-sdk-java

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17.0.10_7-jdk-alpine
WORKDIR /app

# Create a non-root user for security hardening
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR from the build stage
COPY --from=build /app/am-trade-app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

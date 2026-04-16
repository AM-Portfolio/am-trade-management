# Runtime stage using pre-built JAR
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the pre-built JAR file from the correct am-trade-management module
COPY am-trade-app/target/*.jar app.jar

# Set timezone
RUN ln -sf /usr/share/zoneinfo/Asia/Kolkata /etc/localtime

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=Asia/Kolkata

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

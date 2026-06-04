# Multi-stage build for Spring Boot
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy gradle files first for better caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build the application (skip tests for faster deployment)
RUN ./gradlew bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port (Render uses PORT env variable)
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=${PORT:-8080} app.jar"]

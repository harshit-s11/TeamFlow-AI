# Stage 1: Build application with Gradle
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (caching layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build bootJar
COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Production JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user and group for security
RUN addgroup -S teamflow && adduser -S teamflow -G teamflow

# Copy compiled JAR from builder stage
COPY --from=builder --chown=teamflow:teamflow /app/build/libs/backend-0.0.1-SNAPSHOT.jar /app/app.jar

# Install curl for container health check
RUN apk add --no-cache curl

# Switch to non-root user
USER teamflow

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

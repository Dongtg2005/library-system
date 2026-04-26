# Multi-stage build for better caching and smaller final image
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven wrapper and configuration first
COPY mvnw .
RUN chmod +x mvnw
COPY .mvn .mvn/
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install curl for health check
RUN apk add --no-cache curl

# Create app user
RUN addgroup -S appuser && adduser -S appuser -G appuser -h /app -u 1001

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/library-system-*.jar app.jar

# Create uploads directory and change ownership to appuser
RUN mkdir -p /app/uploads/covers && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8080/actuator/health || wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication"

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

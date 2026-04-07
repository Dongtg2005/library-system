FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/library-system-1.0.0.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21

WORKDIR /app

COPY target/book-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]

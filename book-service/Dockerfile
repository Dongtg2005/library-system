FROM eclipse-temurin:21

WORKDIR /app

COPY target/book-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]

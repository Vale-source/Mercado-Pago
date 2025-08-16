FROM openjdk:21-jdk-slim

WORKDIR /app

# Copiar el JAR
COPY target/modulo_mp-0.0.1-SNAPSHOT.jar app.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

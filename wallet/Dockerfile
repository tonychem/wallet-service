FROM amazoncorretto:17.0.0-alpine
COPY target/*.jar wallet-service.jar
ENTRYPOINT ["java", "-jar", "wallet-service.jar"]
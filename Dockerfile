FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar medibot.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "medibot.jar"]

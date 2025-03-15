FROM openjdk:17-jdk-slim
WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

COPY src/main/resources/application.yml /app/config/application.yml

EXPOSE 8081

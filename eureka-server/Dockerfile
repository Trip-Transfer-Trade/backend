FROM openjdk:17-jdk-slim

WORKDIR /app

# JAR 파일 이름 수정
COPY build/libs/eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar

# 실행 명령어
CMD ["java", "-jar", "eureka-server.jar"]

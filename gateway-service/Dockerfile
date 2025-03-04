# OpenJDK 17을 사용
FROM openjdk:17-jdk-slim
WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 컨테이너 실행 시 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# API Gateway는 8080 포트에서 실행됨
EXPOSE 8080

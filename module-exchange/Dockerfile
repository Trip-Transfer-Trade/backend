# 1. OpenJDK 17 Slim 이미지 사용
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 한국 수출입은행 SSL 인증서 복사 (컨테이너 내부 이동)
COPY koreaexim.crt /usr/local/share/ca-certificates/koreaexim.crt

# 4. 인증서를 Java cacerts에 추가
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit -noprompt -alias koreaexim \
    -file /usr/local/share/ca-certificates/koreaexim.crt

# 5. JAR 파일 복사
COPY build/libs/*.jar app.jar

# 6. 컨테이너 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# 7. 외부 접근 포트 설정
EXPOSE 8083

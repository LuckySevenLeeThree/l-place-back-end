# 1. OpenJDK 기반 이미지
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. JAR 파일 복사
COPY build/libs/*.jar app.jar

# 4. 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
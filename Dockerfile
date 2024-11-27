# 1. OpenJDK 기반 이미지
FROM openjdk:17-jdk-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. JAR 파일 복사
COPY build/libs/*.jar app.jar

# 4. 애플리케이션 실행 명령어를 실행하여 컨테이너화
ENTRYPOINT ["java", "-jar", "app.jar"]
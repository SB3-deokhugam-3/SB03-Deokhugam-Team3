# 멀티스테이지 1단계: builder
# 1. 베이스 이미지 선택
FROM amazoncorretto:17 AS builder
# 2. 작업 디렉토리 설정
WORKDIR /app
# 3. Gradle Wrapper와 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN ./gradlew bootJar

# 멀티스테이지 2단계: runtime
# 1. 베이스 이미지 선택

# 2. 런타임
# 환경 변수 설정

FROM amazoncorretto:17 AS runtime

RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    tesseract-ocr-kor \
    && rm -rf /var/lib/apt/lists/*

ENV PROJECT_NAME=${PROJECT_NAME}
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 스테이지에서 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar -> 와카로 처리해도 되지 않나요?

# 80 포트 노출
EXPOSE 80

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
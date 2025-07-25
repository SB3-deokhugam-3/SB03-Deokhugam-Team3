# 1단계: 빌드용 이미지 (Ubuntu + JDK + Tesseract)
FROM ubuntu:20.04 AS builder

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y \
        curl \
        openjdk-17-jdk \
        tesseract-ocr \
        tesseract-ocr-eng \
        tesseract-ocr-kor \
        libtesseract-dev \
        libleptonica-dev \
    && apt-get clean

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar


# 2단계: 실행용 이미지 (Ubuntu + 필수 라이브러리만 복사)
FROM ubuntu:20.04 AS runtime

ENV DEBIAN_FRONTEND=noninteractive

# Tesseract 실행 및 네이티브 라이브러리 실행에 필요한 파일 설치
RUN apt-get update && \
    apt-get install -y \
        tesseract-ocr \
        tesseract-ocr-eng \
        tesseract-ocr-kor \
        libtesseract-dev \
        libleptonica-dev \
        openjdk-17-jdk \
    && apt-get clean

# JVM이 네이티브 라이브러리를 찾을 수 있도록 경로 지정
ENV LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu

WORKDIR /app

# JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 80

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
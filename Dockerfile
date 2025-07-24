# 멀티스테이지 1단계: builder
# 1. 베이스 이미지 선택
FROM amazoncorretto:17 AS builder

# 1. 기본 도구 설치
RUN yum install -y curl rpm && yum clean all

# 2. EPEL 등록 및 Tesseract 설치
RUN curl -LO https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm \
    && rpm -ivh epel-release-latest-7.noarch.rpm \
    && yum install -y tesseract tesseract-langpack-eng \
    && yum clean all

# 3. kor 언어팩 수동 다운로드
RUN mkdir -p /usr/share/tessdata \
    && curl -L -o /usr/share/tessdata/kor.traineddata \
       https://github.com/tesseract-ocr/tessdata/raw/main/kor.traineddata

# 2. 작업 디렉토리 설정
WORKDIR /app
# 3. Gradle Wrapper와 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar

# 멀티스테이지 2단계: runtime
# 1. 베이스 이미지 선택

# 2. 런타임
# 환경 변수 설정

FROM amazoncorretto:17 AS runtime

COPY --from=builder /usr/bin/tesseract /usr/bin/tesseract
COPY --from=builder /usr/share/tessdata /usr/share/tessdata

#RUN yum -y update && \
#    yum install -y amazon-linux-extras && \
#    amazon-linux-extras enable epel && \
#    yum clean metadata && \
#    yum -y install \
#      tesseract \
#      tesseract-langpack-eng \
#      tesseract-langpack-kor && \
#    yum clean all && \
#    rm -rf /var/cache/yum

#RUN apt-get update && apt-get install -y \
#    tesseract-ocr \
#    tesseract-ocr-eng \
#    tesseract-ocr-kor \
#    && rm -rf /var/lib/apt/lists/*



ENV PROJECT_NAME=${PROJECT_NAME}
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=${JVM_OPTS}

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 스테이지에서 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 80 포트 노출
EXPOSE 80

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
# syntax=docker/dockerfile:1

FROM gradle:8.8-jdk21 AS builder
WORKDIR /app

# gradle 관련 먼저 복사해서 빌드 속도 향상
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon dependencies || true

COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon clean bootJar  # 자바 실행용 bootJar 파일 생성

FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]

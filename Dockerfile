# 빌드 환경 설정
FROM gradle:8.7-jdk17 AS build
WORKDIR /workspace

# gradle 캐시 효율을 위해 먼저 설정 파일 복사
COPY build.gradle settings.gradle /workspace/
COPY api/build.gradle api/build.gradle
COPY common/build.gradle common/build.gradle
COPY user/build.gradle user/build.gradle
COPY board/build.gradle board/build.gradle
COPY interaction/build.gradle interaction/build.gradle
COPY chat/build.gradle chat/build.gradle

# 소스 복사
COPY . /workspace

# 실행 가능한 api bootJar 생성
RUN chmod +x ./gradlew && ./gradlew :api:bootJar -x test \
  && set -e; \
     BOOTJAR="$(ls -1 /workspace/api/build/libs/*-boot.jar 2>/dev/null | head -n 1 || true)"; \
     if [ -n "$BOOTJAR" ]; then cp "$BOOTJAR" /workspace/api/build/libs/app.jar; else \
       JAR="$(ls -1 /workspace/api/build/libs/*.jar | head -n 1)"; \
       cp "$JAR" /workspace/api/build/libs/app.jar; \
     fi

# 실행 단계
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 결과 jar 복사
COPY --from=build /workspace/api/build/libs/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
# 멀티모듈 서비스(order-service/performance-service/user-service) 공용 Dockerfile.
# 빌드 컨텍스트는 반드시 레포 루트여야 함 (멀티모듈이라 common 등을 같이 복사해야 함)
# 실행 예: docker build --build-arg SERVICE_NAME=order-service --build-arg SERVICE_PORT=8082 -t order-service .

# ---- build stage ----
FROM eclipse-temurin:21-jdk-jammy AS build

ARG SERVICE_NAME

WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew ":${SERVICE_NAME}:bootJar" -x test --no-daemon

# plain jar(-plain.jar, 실행 불가)와 bootJar가 같이 나오므로, 실행 가능한 bootJar만 골라서 고정 경로로 복사
RUN JAR_FILE=$(find "${SERVICE_NAME}/build/libs" \
        -type f \
        -name "*.jar" \
        ! -name "*-plain.jar" \
        | head -n 1) \
    && test -n "${JAR_FILE}" \
    && cp "${JAR_FILE}" /app/app.jar

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-jammy

ARG SERVICE_PORT

WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE ${SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]

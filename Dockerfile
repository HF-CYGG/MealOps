# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ENV TZ=Asia/Shanghai \
    MEALOPS_UPLOAD_DIR=/app/uploads \
    LOG_PATH=/app/logs

RUN addgroup -S mealops \
    && adduser -S mealops -G mealops \
    && mkdir -p /app/uploads /app/logs \
    && chown -R mealops:mealops /app

COPY --from=build /workspace/target/mealops-*.jar /app/app.jar

USER mealops
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8080/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

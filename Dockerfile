FROM public.ecr.aws/docker/library/node:22-alpine AS frontend-build
WORKDIR /workspace/frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend ./
RUN npm run build

FROM public.ecr.aws/docker/library/maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
COPY sql ./sql
COPY --from=frontend-build /workspace/frontend/dist ./src/main/resources/static
RUN mvn -B -DskipTests package

FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre-alpine
WORKDIR /app

ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    MEALOPS_UPLOAD_DIR=/app/uploads \
    LOG_PATH=/app/logs \
    JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=20.0 -XX:MaxRAMPercentage=70.0 -Dfile.encoding=UTF-8"

RUN addgroup -S mealops \
    && adduser -S mealops -G mealops \
    && mkdir -p /app/uploads /app/logs \
    && chown -R mealops:mealops /app

COPY --from=build /workspace/target/mealops-*.jar /app/app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8080/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "mkdir -p /app/uploads /app/logs && chown -R mealops:mealops /app/uploads /app/logs && exec su mealops -s /bin/sh -c 'java -jar /app/app.jar'"]

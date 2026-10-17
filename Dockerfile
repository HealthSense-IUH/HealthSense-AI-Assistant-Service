# ========================
# Stage 1: build bằng Maven, cache dependency theo pom.xml
# ========================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package \
    && cp target/*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# ========================
# Stage 2: runtime chỉ có JRE, chạy non-root
# ========================
FROM eclipse-temurin:21-jre

# curl cho HEALTHCHECK; user non-root
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Chép theo layer để lần build sau chỉ đổi layer application
COPY --from=builder --chown=appuser:appuser /build/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appuser /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appuser /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /build/extracted/application/ ./

USER appuser

ENV SERVER_PORT=8081 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:8081/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

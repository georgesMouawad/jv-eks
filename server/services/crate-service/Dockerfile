# ── Stage 1: Build fat JAR + custom minimal JRE ─────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties* ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q 2>/dev/null || true

# Copy build files for ALL subprojects — root settings.gradle.kts includes all
# five, so Gradle requires every declared project directory to exist at config time.
COPY services/auth-service/build.gradle.kts  ./services/auth-service/build.gradle.kts
COPY services/user-service/build.gradle.kts  ./services/user-service/build.gradle.kts
COPY services/api-gateway/build.gradle.kts   ./services/api-gateway/build.gradle.kts
COPY services/crate-service/build.gradle.kts ./services/crate-service/build.gradle.kts
COPY services/sync-service/build.gradle.kts  ./services/sync-service/build.gradle.kts
COPY services/crate-service/src/ services/crate-service/src/
RUN ./gradlew :services:crate-service:bootJar --no-daemon -x test

# Build a minimal JRE containing modules required by Spring Boot + JPA + Security + AWS SDK
RUN $JAVA_HOME/bin/jlink \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.xml,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.jfr,jdk.management,jdk.management.agent,jdk.naming.dns,jdk.naming.rmi,jdk.unsupported,jdk.zipfs \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /jre-minimal

# ── Stage 2: Extract Spring Boot layers ─────────────────────────────────────
FROM alpine:3.21 AS extractor
COPY --from=builder /jre-minimal /opt/java
ENV PATH="/opt/java/bin:$PATH"
WORKDIR /app
COPY --from=builder /app/services/crate-service/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ── Stage 3: Minimal runtime image ───────────────────────────────────────────
FROM alpine:3.21
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /jre-minimal /opt/java
ENV PATH="/opt/java/bin:$PATH" \
    JAVA_HOME="/opt/java"

USER spring:spring

# Layers ordered least-to-most volatile for cache efficiency
COPY --from=extractor /app/extracted/dependencies/          ./
COPY --from=extractor /app/extracted/spring-boot-loader/    ./
COPY --from=extractor /app/extracted/snapshot-dependencies/ ./
COPY --from=extractor /app/extracted/application/           ./

EXPOSE 8083

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]

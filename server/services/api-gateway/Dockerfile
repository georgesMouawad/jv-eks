# ── Stage 1: Build fat JAR ──────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties* ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q 2>/dev/null || true

# Copy build files for ALL subprojects — root settings.gradle.kts includes all three,
# so Gradle requires every declared project directory to exist at configuration time.
COPY services/auth-service/build.gradle.kts  ./services/auth-service/build.gradle.kts
COPY services/user-service/build.gradle.kts  ./services/user-service/build.gradle.kts
COPY services/api-gateway/build.gradle.kts   ./services/api-gateway/build.gradle.kts
# Copy only this service's source
COPY services/api-gateway/src/ services/api-gateway/src/
RUN ./gradlew :services:api-gateway:bootJar --no-daemon -x test

# ── Stage 2: Extract Spring Boot layers ─────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS extractor
WORKDIR /app
COPY --from=builder /app/services/api-gateway/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ── Stage 3: Minimal runtime image ──────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=extractor /app/extracted/dependencies/          ./
COPY --from=extractor /app/extracted/spring-boot-loader/    ./
COPY --from=extractor /app/extracted/snapshot-dependencies/ ./
COPY --from=extractor /app/extracted/application/           ./

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]

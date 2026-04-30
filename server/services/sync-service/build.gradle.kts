plugins {
	java
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.devops.sync"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	// WebFlux provides reactive WebSocket support (non-blocking, better for
	// many concurrent persistent connections)
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	// Lettuce (included in data-redis) provides the reactive connection factory
	// used by ReactiveStringRedisTemplate for channel subscriptions
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.devops.common"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.6")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Exposed as API so consuming services can use JwtAuthentication / BeanFactoryPostProcessor
    // on their own compile classpaths without re-declaring these deps.
    api("org.springframework.security:spring-security-web")
    api("org.springframework:spring-context")
    compileOnly("jakarta.servlet:jakarta.servlet-api")

    // jjwt — only JwtService in this lib calls the API directly;
    // impl + jackson are runtimeOnly so they propagate to consuming services' runtime classpath.
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}

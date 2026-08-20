import org.gradle.api.tasks.testing.Test // Allows configuring JUnit test tasks.

plugins {
    kotlin("jvm") version "2.3.21" // Enables Kotlin JVM compilation plugin.
    kotlin("plugin.spring") version "2.3.21" // Opens Spring managed Kotlin classes for proxies.
    id("org.springframework.boot") version "4.1.0" // Spring Boot packaging and execution tasks plugin.
    id("io.spring.dependency-management") version "1.1.7" // Resolves compatible Spring Boot dependency versions.
}

group = "com.example.processor" // Package group metadata.
version = "0.0.1-SNAPSHOT" // Application release version.

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // Targets Java 21 LTS runtime toolchain.
    }
}

repositories {
    mavenCentral() // Fetches dependencies from Maven Central repository.
}

dependencies {
    developmentOnly("org.springframework.boot:spring-boot-devtools") // Development hot-reloading tools.

    implementation("org.springframework.kafka:spring-kafka") // Spring Kafka starter for EventHub producer and consumer operations.
    implementation("org.springframework.boot:spring-boot-starter-actuator") // Health and metrics monitoring endpoints starter.
    implementation("org.springframework.boot:spring-boot-starter-webmvc") // Web MVC starter for embedded Tomcat web server.
    implementation("org.jetbrains.kotlin:kotlin-reflect") // Reflective library support for Kotlin classes.
    implementation("tools.jackson.module:jackson-module-kotlin") // Jackson JSON serialization/deserialization for Kotlin data classes.

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test") // Test utilities for actuator metrics.
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test") // Test utilities for actuator metrics.
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test") // Test framework for Web MVC layer.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5") // JUnit 5 integration library for Kotlin.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // JUnit platform execution launcher engine.

    // Logging & Micrometer Tracing
    implementation("org.springframework.boot:spring-boot-micrometer-tracing") // Micrometer tracing abstraction starter.
    implementation("org.springframework.boot:spring-boot-micrometer-tracing-brave") // Brave tracing bridge.
    implementation("io.micrometer:micrometer-tracing") // Micrometer tracing core library.
    implementation("io.micrometer:micrometer-tracing-bridge-brave") // Micrometer Brave bridge module.
}

kotlin {
    compilerOptions {
        // Enforces strict JSR-305 nullability checking and param-property annotation defaults.
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property"
        )
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform() // Executes tests using JUnit 5 test platform.
}

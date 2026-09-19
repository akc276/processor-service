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

    // Core Starters
    implementation("org.springframework.boot:spring-boot-starter-webmvc") // Web MVC starter for embedded Tomcat server
    implementation("org.springframework.boot:spring-boot-starter-actuator") // Health and metrics monitoring endpoints starter
    implementation("org.springframework.kafka:spring-kafka") // Spring Kafka starter for EventHub operations

    // Kotlin Reflection & Jackson
    implementation("org.jetbrains.kotlin:kotlin-reflect") // Reflective library support for Kotlin classes
    implementation("tools.jackson.module:jackson-module-kotlin") // Jackson JSON serialization for Kotlin

    // Distributed Tracing
    implementation("org.springframework.boot:spring-boot-micrometer-tracing") // Enables Spring Boot Tracer auto-configuration
    implementation("org.springframework.boot:spring-boot-micrometer-tracing-brave") // Enables Spring Boot Brave auto-configuration
    implementation("io.micrometer:micrometer-tracing-bridge-brave") // Brave bridge for W3C trace context & Kafka headers
    implementation("io.zipkin.contrib.brave:brave-kafka-interceptor:0.6.0") // Brave interceptor classes for Kafka producer & consumer headers

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5") // JUnit 5 integration library for Kotlin
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
